package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.ActualizarPerfilRequest;
import app.controller.dto.UsuarioResponse;
import app.model.Usuario;
import app.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Datos del usuario autenticado, para que el front los cargue después del login (issue #54). */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerMiPerfil(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorEmail(authentication.getName());
        return ApiResponse.ok("Usuario obtenido", new UsuarioResponse(usuario));
    }

    @PatchMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarPerfil(
            @RequestBody ActualizarPerfilRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Usuario actualizado = usuarioService.actualizarPerfil(email, request);
        return ApiResponse.ok("Perfil actualizado", new UsuarioResponse(actualizado));
    }
}