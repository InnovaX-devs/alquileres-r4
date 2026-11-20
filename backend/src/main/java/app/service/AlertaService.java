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

@Service
public class AlertaService {

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

        return alertaRepository.save(alerta);
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
        return alertaRepository.save(alerta);
    }

    @Transactional
    public void eliminarAlerta(Long alertaId, String emailUsuario) {
        Alerta alerta = obtenerAlertaPropia(alertaId, buscarUsuario(emailUsuario));
        alertaRepository.delete(alerta);
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permisos sobre esta alerta");
        }

        return alerta;
    }

    public void evaluarAlertas(Propiedad propiedad) {
        List<Alerta> alertasActivas = alertaRepository.findByActivaTrue();

        for (Alerta alerta : alertasActivas) {
            if (coincide(propiedad, alerta)) {
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
                } catch (Exception e) {
                    System.err.println("Error al enviar email de alerta: " + e.getMessage());
                }
            }
        }
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