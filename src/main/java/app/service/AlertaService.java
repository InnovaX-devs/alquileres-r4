package app.service;

import app.controller.dto.AlertaRequest;
import app.model.Alerta;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.AlertaRepository;
import app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;

    public AlertaService(AlertaRepository alertaRepository,
                         UsuarioRepository usuarioRepository) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Alerta crearAlerta(AlertaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Alerta alerta = new Alerta();
        alerta.setUsuario(usuario);
        alerta.setPrecioMin(request.getPrecioMin());
        alerta.setPrecioMax(request.getPrecioMax());
        alerta.setMoneda(request.getMoneda());
        alerta.setHabitacionesMin(request.getHabitacionesMin());
        alerta.setCalle(request.getCalle());

        return alertaRepository.save(alerta);
    }

    public void evaluarAlertas(Propiedad propiedad) {
        List<Alerta> alertasActivas = alertaRepository.findByActivaTrue();

        for (Alerta alerta : alertasActivas) {
            if (coincide(propiedad, alerta)) {
                // por ahora solo log, el email va en el próximo issue
                System.out.println("Alerta coincidente para usuario: " 
                    + alerta.getUsuario().getEmail());
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