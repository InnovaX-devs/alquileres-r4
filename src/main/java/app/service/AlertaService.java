package app.service;

import app.controller.dto.AlertaRequest;
import app.model.Alerta;
import app.model.Usuario;
import app.repository.AlertaRepository;
import app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

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
}