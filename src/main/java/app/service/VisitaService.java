package app.service;

import app.controller.dto.SolicitudVisitaRequest;
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
}