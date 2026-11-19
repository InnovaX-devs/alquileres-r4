package app.service;

import app.controller.dto.CambiarEstadoVisitaRequest;
import app.controller.dto.SolicitudVisitaRequest;
import app.model.EstadoSolicitud;
import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
public class VisitaService {

    private final SolicitudVisitaRepository solicitudVisitaRepository;
    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public VisitaService(SolicitudVisitaRepository solicitudVisitaRepository,
                         PropiedadRepository propiedadRepository,
                         UsuarioRepository usuarioRepository,
                         EmailService emailService) {
        this.solicitudVisitaRepository = solicitudVisitaRepository;
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    public SolicitudVisita solicitarVisita(SolicitudVisitaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Propiedad propiedad = propiedadRepository.findById(request.getPropiedadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"));

        boolean yaExiste = solicitudVisitaRepository
                .existsByUsuarioAndPropiedad(usuario, propiedad);

        if (yaExiste) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tenés una solicitud para esta propiedad");
        }

        SolicitudVisita solicitud = new SolicitudVisita();
        solicitud.setUsuario(usuario);
        solicitud.setPropiedad(propiedad);
        solicitud.setFechaPropuesta(request.getFechaPropuesta());
        solicitud.setObservaciones(request.getObservaciones());
        
        SolicitudVisita guardada = solicitudVisitaRepository.save(solicitud);

        try {
            String emailPublicador;
            String nombrePublicador;

            if (propiedad.getInmobiliaria() != null) {
                emailPublicador = propiedad.getInmobiliaria().getEmail();
                nombrePublicador = propiedad.getInmobiliaria().getNombre();
            } else {
                emailPublicador = propiedad.getUsuario().getEmail();
                nombrePublicador = propiedad.getUsuario().getNombre();
            }

            emailService.enviarNotificacionVisita(
                emailPublicador,
                nombrePublicador,
                propiedad.getTitulo(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                request.getObservaciones()
            );
        } catch (Exception e) {
            System.err.println("Error al enviar email de notificación: " + e.getMessage());
        }

        return guardada;
    }

    @Transactional
    public SolicitudVisita cambiarEstado(Long solicitudId, CambiarEstadoVisitaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        SolicitudVisita solicitud = solicitudVisitaRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        Propiedad propiedad = solicitud.getPropiedad();

        boolean esPublicador = esPublicadorDeLaPropiedad(usuario, propiedad);

        if (!esPublicador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permisos para modificar esta solicitud");
        }

        if (request.getEstado() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        if (request.getEstado() == EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No podés volver el estado a PENDIENTE");
        }

        solicitud.setEstado(request.getEstado());

        return solicitudVisitaRepository.save(solicitud);
    }

    /**
     * Es publicador quien creó la propiedad o quien pertenece a la inmobiliaria que la publica.
     * Se evalúan ambas condiciones: el contacto que se muestra prioriza a la inmobiliaria,
     * así que sus miembros también deben poder gestionar las solicitudes.
     */
    private boolean esPublicadorDeLaPropiedad(Usuario usuario, Propiedad propiedad) {
        boolean esDuenio = propiedad.getUsuario() != null
                && propiedad.getUsuario().getId().equals(usuario.getId());

        boolean esDeLaInmobiliaria = propiedad.getInmobiliaria() != null
                && usuario.getInmobiliaria() != null
                && propiedad.getInmobiliaria().getId().equals(usuario.getInmobiliaria().getId());

        return esDuenio || esDeLaInmobiliaria;
    }
}
