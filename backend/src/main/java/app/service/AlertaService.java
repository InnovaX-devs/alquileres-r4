package app.service;

import app.controller.dto.AlertaRequest;
import app.model.Alerta;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.AlertaRepository;
import app.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AlertaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaService.class);

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public AlertaService(AlertaRepository alertaRepository,
                        UsuarioRepository usuarioRepository,
                        EmailService emailService) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    public Alerta crearAlerta(AlertaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Alerta alerta = new Alerta();
        alerta.setUsuario(usuario);
        alerta.setPrecioMin(request.getPrecioMin());
        alerta.setPrecioMax(request.getPrecioMax());
        alerta.setMoneda(request.getMoneda());
        alerta.setHabitacionesMin(request.getHabitacionesMin());
        alerta.setCalle(request.getCalle());

        Alerta guardada = alertaRepository.save(alerta);
        log.info("Alerta creada: id={}, usuarioId={}", guardada.getId(), usuario.getId());
        return guardada;
    }

    // ── Gestión de alertas (issue #60) ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Alerta> listarAlertas(String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        return alertaRepository.findByUsuarioIdOrderByCreadaEnDesc(usuario.getId());
    }

    @Transactional
    public Alerta cambiarEstado(Long alertaId, boolean activa, String emailUsuario) {
        Alerta alerta = obtenerAlertaPropia(alertaId, buscarUsuario(emailUsuario));
        alerta.setActiva(activa);
        Alerta guardada = alertaRepository.save(alerta);
        log.info("Alerta {}: id={}", activa ? "activada" : "desactivada", alertaId);
        return guardada;
    }

    @Transactional
    public void eliminarAlerta(Long alertaId, String emailUsuario) {
        Alerta alerta = obtenerAlertaPropia(alertaId, buscarUsuario(emailUsuario));
        alertaRepository.delete(alerta);
        log.info("Alerta eliminada: id={}", alertaId);
    }

    private Usuario buscarUsuario(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    /** 404 si la alerta no existe, 403 si existe pero es de otro usuario. */
    private Alerta obtenerAlertaPropia(Long alertaId, Usuario usuario) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));

        boolean esDuenio = alerta.getUsuario() != null
                && alerta.getUsuario().getId().equals(usuario.getId());

        if (!esDuenio) {
            log.warn("Acceso denegado a alerta: alertaId={}, usuarioId={}", alertaId, usuario.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permisos sobre esta alerta");
        }

        return alerta;
    }

            public void evaluarAlertas(Propiedad propiedad) {
        List<Alerta> alertasActivas = alertaRepository.findByActivaTrue();
        log.info("Evaluando alertas para propiedadId={}: {} alertas activas",
                propiedad.getId(), alertasActivas.size());

        int coincidencias = 0;
        for (Alerta alerta : alertasActivas) {
            if (coincide(propiedad, alerta)) {
                coincidencias++;
                try {
                    emailService.enviarNotificacionAlerta(
                        alerta.getUsuario().getEmail(),
                        alerta.getUsuario().getNombre(),
                        propiedad.getTitulo(),
                        propiedad.getCalle(),
                        propiedad.getAltura(),
                        propiedad.getPrecio(),
                        propiedad.getMoneda() != null ? propiedad.getMoneda().name() : ""
                    );
                    log.info("Alerta notificada: alertaId={}, propiedadId={}", alerta.getId(), propiedad.getId());
                } catch (Exception e) {
                    log.error("Error al enviar email de alerta: alertaId={}, propiedadId={}",
                            alerta.getId(), propiedad.getId(), e);
                }
            }
        }

        log.info("Evaluación de alertas terminada: propiedadId={}, coincidencias={}",
                propiedad.getId(), coincidencias);
    }
    

    private boolean coincide(Propiedad propiedad, Alerta alerta) {
        if (alerta.getPrecioMin() != null && propiedad.getPrecio() != null
                && propiedad.getPrecio().compareTo(alerta.getPrecioMin()) < 0) {
            return false;
        }

        if (alerta.getPrecioMax() != null && propiedad.getPrecio() != null
                && propiedad.getPrecio().compareTo(alerta.getPrecioMax()) > 0) {
            return false;
        }

        if (alerta.getMoneda() != null && propiedad.getMoneda() != null
                && !propiedad.getMoneda().equals(alerta.getMoneda())) {
            return false;
        }

        if (alerta.getHabitacionesMin() != null && propiedad.getHabitaciones() != null
                && propiedad.getHabitaciones() < alerta.getHabitacionesMin()) {
            return false;
        }

        if (alerta.getCalle() != null && propiedad.getCalle() != null
                && !propiedad.getCalle().toLowerCase().contains(alerta.getCalle().toLowerCase())) {
            return false;
        }

        return true;
    }
}