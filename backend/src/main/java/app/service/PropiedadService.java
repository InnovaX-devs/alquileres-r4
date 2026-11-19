package app.service;

import app.controller.dto.ContactoPublicadorResponse;
import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.Fuente;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PropiedadService {

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

        try {
            alertaService.evaluarAlertas(guardada);
        } catch (Exception e) {
            System.err.println("Error al evaluar alertas: " + e.getMessage());
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
        return propiedadRepository.findAll()
                .stream()
                .map(propiedad -> PropiedadDetalleResponse.from(propiedad, null))
                .toList();
    }
}