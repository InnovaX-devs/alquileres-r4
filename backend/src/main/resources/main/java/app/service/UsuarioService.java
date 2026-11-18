package app.service;

import app.controller.dto.ActualizarPerfilRequest;
import app.model.Usuario;
import app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario actualizarPerfil(String email, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getDocumento() != null) {
            usuario.setDocumento(request.getDocumento());
        }
        if (request.getDescripcionPerfil() != null) {
            usuario.setDescripcionPerfil(request.getDescripcionPerfil());
        }

        usuario.setVerificado(perfilEstaCompleto(usuario));

        return usuarioRepository.save(usuario);
    }

    private boolean perfilEstaCompleto(Usuario usuario) {
        return notBlank(usuario.getNombre())
                && notBlank(usuario.getTelefono())
                && notBlank(usuario.getDocumento());
    }

    private boolean notBlank(String valor) {
        return valor != null && !valor.isBlank();
    }
}