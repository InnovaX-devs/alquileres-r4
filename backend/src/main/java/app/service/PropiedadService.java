package app.service;

import app.controller.dto.ContactoPublicadorResponse;
import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.EstadoPropiedad;
import app.model.Fuente;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PropiedadService {

    private static final Logger log = LoggerFactory.getLogger(PropiedadService.class);

    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlertaService alertaService;


    public PropiedadService(PropiedadRepository propiedadRepository,
                            UsuarioRepository usuarioRepository,
                            AlertaService alertaService) {
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.alertaService = alertaService;
    }

    public Propiedad publicar(PropiedadRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        // Sin fecha, o desde hoy en adelante (issue #154)
        request.validarDisponibleDesde(null, Propiedad.hoy());

        Propiedad propiedad = new Propiedad();
        request.aplicarA(propiedad);
        propiedad.setFuente(Fuente.MANUAL);
        propiedad.setUsuario(usuario);
        // Si el usuario pertenece a una inmobiliaria, la propiedad se publica a su nombre (issue #61).
        propiedad.setInmobiliaria(usuario.getInmobiliaria());

        Propiedad guardada = propiedadRepository.save(propiedad);
        log.info("Propiedad publicada: id={}, usuarioId={}, inmobiliariaId={}, fuente={}",
                guardada.getId(), usuario.getId(),
                guardada.getInmobiliaria() != null ? guardada.getInmobiliaria().getId() : null,
                guardada.getFuente());

        try {
            alertaService.evaluarAlertas(guardada);
        } catch (Exception e) {
            log.error("Error al evaluar alertas para la propiedad id={}", guardada.getId(), e);
        }

        return guardada;
    }

    // ── Detalle de propiedad ──────────────────────────────────────────────────

    // readOnly: el DTO recorre la colección lazy de imágenes y con open-in-view=false
    // necesita una sesión abierta para cargarla (issue #59).
    @Transactional(readOnly = true)
    public PropiedadDetalleResponse getDetalle(Long id, String emailAutenticado) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"));

        ContactoPublicadorResponse contacto = resolverContacto(propiedad, emailAutenticado);

        return PropiedadDetalleResponse.from(propiedad, contacto);
    }

    private ContactoPublicadorResponse resolverContacto(Propiedad propiedad, String emailAutenticado) {
        if (emailAutenticado == null) return null;

        Usuario solicitante = usuarioRepository.findByEmail(emailAutenticado).orElse(null);

        if (solicitante == null || !solicitante.isVerificado()) return null;

        if (propiedad.getInmobiliaria() != null) {
            return ContactoPublicadorResponse.fromInmobiliaria(propiedad.getInmobiliaria());
        }

        if (propiedad.getUsuario() != null) {
            return ContactoPublicadorResponse.fromUsuario(propiedad.getUsuario());
        }

        return null;
    }

    // ── Mis propiedades (issue #55) ──────────────────────────────────────────

    /**
     * Propiedades del publicador autenticado: las que publicó como particular y, si pertenece a una
     * inmobiliaria, todas las de esa inmobiliaria (mismo criterio que Propiedad#esPublicadaPor).
     */
    @Transactional(readOnly = true)
    public List<PropiedadDetalleResponse> listarMias(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        List<Propiedad> propiedades = usuario.getInmobiliaria() != null
                ? propiedadRepository.findDelUsuarioOSuInmobiliaria(usuario.getId(), usuario.getInmobiliaria().getId())
                : propiedadRepository.findByUsuarioIdAndInmobiliariaIsNullOrderByIdDesc(usuario.getId());

        // El contacto es el propio publicador: no hace falta mostrarlo.
        return propiedades.stream()
                .map(PropiedadDetalleResponse::paraPublicador)
                .toList();
    }

    // Listar propiedades para usuarios sin autenticar
    public List<PropiedadDetalleResponse> listar() {
        return listar(null, null, null, null, null);
    }

    /** Listado sin el filtro de disponibilidad. */
    public List<PropiedadDetalleResponse> listar(BigDecimal precioMin, BigDecimal precioMax, Moneda moneda,
                                                 Integer habitacionesMin, String calle) {
        return listar(precioMin, precioMax, moneda, habitacionesMin, calle, null);
    }

    /**
     * Listado público con filtros opcionales (issue #58). Los filtros que vienen null o vacíos se ignoran
     * y los que vienen informados se combinan con AND. Solo incluye las propiedades disponibles:
     * sin ningún filtro devuelve todas las que no están alquiladas.
     *
     * {@code disponibleAntesDe} (issue #154) deja las que se pueden ocupar hasta esa fecha inclusive:
     * las que no tienen fecha (disponibles ya) y las que la tienen igual o anterior. "Disponible ahora"
     * es pasar la fecha de hoy.
     */
    @Transactional(readOnly = true)
    public List<PropiedadDetalleResponse> listar(BigDecimal precioMin, BigDecimal precioMax, Moneda moneda,
                                                 Integer habitacionesMin, String calle,
                                                 LocalDate disponibleAntesDe) {
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El precio mínimo no puede ser mayor al precio máximo");
        }

        String calleFiltro = calle != null && !calle.isBlank() ? calle.trim() : null;

        // Las alquiladas no se listan (siguen accesibles en el detalle, por ejemplo desde favoritos)
        List<Propiedad> propiedades = propiedadRepository.findAll(
                filtros(precioMin, precioMax, moneda, habitacionesMin, calleFiltro, disponibleAntesDe));

        return propiedades.stream()
                .map(propiedad -> PropiedadDetalleResponse.from(propiedad, null))
                .toList();
    }

    private Specification<Propiedad> filtros(BigDecimal precioMin, BigDecimal precioMax, Moneda moneda,
                                             Integer habitacionesMin, String calle,
                                             LocalDate disponibleAntesDe) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            // Disponibles: las publicadas antes de que existiera el estado tienen null
            predicados.add(cb.or(cb.isNull(root.get("estado")),
                    cb.equal(root.get("estado"), EstadoPropiedad.DISPONIBLE)));

            if (precioMin != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }
            if (precioMax != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }
            if (moneda != null) {
                predicados.add(cb.equal(root.get("moneda"), moneda));
            }
            if (habitacionesMin != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("habitaciones"), habitacionesMin));
            }
            if (calle != null) {
                predicados.add(cb.like(cb.lower(root.get("calle")), "%" + calle.toLowerCase() + "%"));
            }
            if (disponibleAntesDe != null) {
                // Sin fecha cuenta como disponible ya
                predicados.add(cb.or(cb.isNull(root.get("disponibleDesde")),
                        cb.lessThanOrEqualTo(root.get("disponibleDesde"), disponibleAntesDe)));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
