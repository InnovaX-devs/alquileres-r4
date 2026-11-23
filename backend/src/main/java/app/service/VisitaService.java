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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class VisitaService {

        private static final Logger log = LoggerFactory.getLogger(AlertaService.class);

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
            log.warn("Solicitud de visita duplicada: usuarioId={}, propiedadId={}",
                    usuario.getId(), propiedad.getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tenés una solicitud para esta propiedad");
        }

        SolicitudVisita solicitud = new SolicitudVisita();
        solicitud.setUsuario(usuario);
        solicitud.setPropiedad(propiedad);
        solicitud.setFechaPropuesta(request.getFechaPropuesta());
        solicitud.setObservaciones(request.getObservaciones());

        SolicitudVisita guardada = solicitudVisitaRepository.save(solicitud);
        log.info("Solicitud de visita creada: id={}, usuarioId={}, propiedadId={}, estado={}",
                guardada.getId(), usuario.getId(), propiedad.getId(), guardada.getEstado());

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
            log.info("Notificación de visita enviada: solicitudId={}", guardada.getId());
        } catch (Exception e) {
            log.error("Error al enviar el email de notificación: solicitudId={}", guardada.getId(), e);
        }

        return guardada;
    }

    /** Solicitudes realizadas por el usuario autenticado (issue #56). */
    @Transactional(readOnly = true)
    public List<SolicitudVisita> listarMias(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        return solicitudVisitaRepository.findRealizadasPorUsuario(usuario.getId());
    }

    /**
     * Solicitudes recibidas por el publicador autenticado (issue #57): las de las propiedades que creó
     * y, si pertenece a una inmobiliaria, las de las propiedades de esa inmobiliaria
     * (mismo criterio de publicador que {@link Propiedad#esPublicadaPor}).
     */
    @Transactional(readOnly = true)
    public List<SolicitudVisita> listarRecibidas(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        return usuario.getInmobiliaria() != null
                ? solicitudVisitaRepository.findRecibidasPorUsuarioOInmobiliaria(
                        usuario.getId(), usuario.getInmobiliaria().getId())
                : solicitudVisitaRepository.findRecibidasPorUsuario(usuario.getId());
    }

    @Transactional
    public SolicitudVisita cambiarEstado(Long solicitudId, CambiarEstadoVisitaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        SolicitudVisita solicitud = solicitudVisitaRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        Propiedad propiedad = solicitud.getPropiedad();

        boolean esPublicador = propiedad.esPublicadaPor(usuario);

        if (!esPublicador) {
            log.warn("Cambio de estado denegado: solicitudId={}, usuarioId={}", solicitudId, usuario.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permisos para modificar esta solicitud");
        }

        if (request.getEstado() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        if (request.getEstado() == EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No podés volver el estado a PENDIENTE");
        }

        EstadoSolicitud anterior = solicitud.getEstado();
        solicitud.setEstado(request.getEstado());
        SolicitudVisita actualizada = solicitudVisitaRepository.save(solicitud);
        log.info("Estado de solicitud actualizado: id={}, {} -> {}, usuarioId={}",
                solicitudId, anterior, actualizada.getEstado(), usuario.getId());

        return actualizada;
    }

}
