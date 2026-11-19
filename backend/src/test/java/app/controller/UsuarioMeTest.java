package app.controller;

import app.exception.GlobalExceptionHandler;
import app.model.Rol;
import app.model.Usuario;
import app.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #54: GET /usuarios/me devuelve los datos del usuario autenticado sin exponer el hash.
 * El 401 sin token lo cubre AccessControlTest.
 */
class UsuarioMeTest {

    private UsuarioService usuarioService;
    private MockMvc mockMvc;
    private final UsernamePasswordAuthenticationToken principal =
            new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void devuelveLosDatosDelUsuarioAutenticado() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setEmail("user@test.com");
        usuario.setTelefono("1122334455");
        usuario.setDocumento("30111222");
        usuario.setDescripcionPerfil("Busco depto");
        usuario.setPasswordHash("$2a$10$hash-secreto");
        usuario.setVerificado(true);
        usuario.setRol(Rol.USER);
        when(usuarioService.obtenerPorEmail("user@test.com")).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/me").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.telefono").value("1122334455"))
                .andExpect(jsonPath("$.documento").value("30111222"))
                .andExpect(jsonPath("$.descripcionPerfil").value("Busco depto"))
                .andExpect(jsonPath("$.verificado").value(true))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void usuarioInexistenteDa401() throws Exception {
        when(usuarioService.obtenerPorEmail("user@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        mockMvc.perform(get("/usuarios/me").principal(principal))
                .andExpect(status().isUnauthorized());
    }
}
