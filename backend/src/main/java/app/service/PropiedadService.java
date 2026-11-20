package app.service;

import app.controller.dto.ContactoPublicadorResponse;
import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
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
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
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

        Propiedad propiedad = new Propiedad();
        propiedad.setTitulo(request.getTitulo());
        propiedad.setDescripcion(request.getDescripcion());
        propiedad.setPrecio(request.getPrecio());
        propiedad.setExpensas(request.getExpensas());
        propiedad.setMoneda(request.getMoneda());
        propiedad.setCalle(request.getCalle());
        propiedad.setAltura(request.getAltura());
        propiedad.setPiso(request.getPiso());
        propiedad.setHabitaciones(request.getHabitaciones());
        propiedad.setMetrosCuadrados(request.getMetrosCuadrados());
        propiedad.setIncluyeImpuestos(request.getIncluyeImpuestos());
        propiedad.setPlazoActualizacionMeses(request.getPlazoActualizacionMeses());
        propiedad.setRequisitosIngreso(request.getRequisitosIngreso());
        propiedad.setFuente(Fuente.MANUAL);
        propiedad.setUsuario(usuario);

        Propiedad guardada = propiedadRepository.save(propiedad);
        log.info("Propiedad publicada: id={}, usuarioId={}, fuente={}",
                guardada.getId(), usuario.getId(), guardada.getFuente());

        try {
            alertaService.evaluarAlertas(guardada);
        } catch (Exception e) {
            log.error("Error al evaluar alertas para la propiedad id={}", guardada.getId(), e);
        }

        return guardada;
    }

    // ── Detalle de propiedad ──────────────────────────────────────────────────

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
     * Propiedades del publicador autenticado: las que creó él y, si pertenece a una inmobiliaria,
     * las de esa inmobiliaria (mismo criterio de publicador que VisitaService).
     */
    public List<PropiedadDetalleResponse> listarMias(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        List<Propiedad> propiedades = usuario.getInmobiliaria() != null
                ? propiedadRepository.findDelUsuarioOSuInmobiliaria(usuario.getId(), usuario.getInmobiliaria().getId())
                : propiedadRepository.findByUsuarioIdOrderByIdDesc(usuario.getId());

        // El contacto es el propio publicador: no hace falta mostrarlo.
        return propiedades.stream()
                .map(propiedad -> PropiedadDetalleResponse.from(propiedad, null))
                .toList();
    }

    // Listar propiedades para usuarios sin autenticar
    public List<PropiedadDetalleResponse> listar() {
        return listar(null, null, null, null, null);
    }

    /**
     * Listado público con filtros opcionales (issue #58). Los filtros que vienen null o vacíos se ignoran
     * y los que vienen informados se combinan con AND. Sin ningún filtro devuelve todas las propiedades.
     */
    public List<PropiedadDetalleResponse> listar(BigDecimal precioMin, BigDecimal precioMax, Moneda moneda,
                                                 Integer habitacionesMin, String calle) {
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El precio mínimo no puede ser mayor al precio máximo");
        }

        String calleFiltro = calle != null && !calle.isBlank() ? calle.trim() : null;
        boolean sinFiltros = precioMin == null && precioMax == null && moneda == null
                && habitacionesMin == null && calleFiltro == null;

        List<Propiedad> propiedades = sinFiltros
                ? propiedadRepository.findAll()
                : propiedadRepository.findAll(filtros(precioMin, precioMax, moneda, habitacionesMin, calleFiltro));

        return propiedades.stream()
                .map(propiedad -> PropiedadDetalleResponse.from(propiedad, null))
                .toList();
    }

    private Specification<Propiedad> filtros(BigDecimal precioMin, BigDecimal precioMax, Moneda moneda,
                                             Integer habitacionesMin, String calle) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

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

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
