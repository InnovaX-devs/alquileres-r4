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
import org.springframework.web.server.ResponseStatusException;


@Service
public class VisitaService {

    private final SolicitudVisitaRepository solicitudVisitaRepository;
    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;

    public VisitaService(SolicitudVisitaRepository solicitudVisitaRepository,
                         PropiedadRepository propiedadRepository,
                         UsuarioRepository usuarioRepository) {
        this.solicitudVisitaRepository = solicitudVisitaRepository;
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
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

        return solicitudVisitaRepository.save(solicitud);
    }

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

    if (request.getEstado() == EstadoSolicitud.PENDIENTE) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No podés volver el estado a PENDIENTE");
    }

    solicitud.setEstado(request.getEstado());

    return solicitudVisitaRepository.save(solicitud);
    }

    private boolean esPublicadorDeLaPropiedad(Usuario usuario, Propiedad propiedad) {
        if (propiedad.getUsuario() != null) {
            return propiedad.getUsuario().getId().equals(usuario.getId());
        }

        if (propiedad.getInmobiliaria() != null && usuario.getInmobiliaria() != null) {
            return propiedad.getInmobiliaria().getId().equals(usuario.getInmobiliaria().getId());
        }

        return false;
    }
}