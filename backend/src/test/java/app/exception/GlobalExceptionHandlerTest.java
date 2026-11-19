package app.exception;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #50: formato y códigos de las respuestas de error.
 * Usa un controller de prueba y el handler real, sin levantar Spring ni la base.
 */
class GlobalExceptionHandlerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDePrueba())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class ControllerDePrueba {
        @GetMapping("/test/conflicto")
        String conflicto() { throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado"); }

        @GetMapping("/test/no-encontrado")
        String noEncontrado() { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"); }

        @GetMapping("/test/integridad")
        String integridad() { throw new DataIntegrityViolationException("duplicate key value violates constraint x"); }

        @GetMapping("/test/inesperado")
        String inesperado() { throw new IllegalStateException("detalle interno que no debe filtrarse"); }

        @GetMapping("/test/prohibido")
        String prohibido() { throw new AccessDeniedException("sin permisos"); }

        @GetMapping("/test/{id}")
        String porId(@PathVariable Long id) { return "ok"; }

        @PostMapping("/test/body")
        String body(@RequestBody java.util.Map<String, String> body) { return "ok"; }
    }

    @Test
    void responseStatusExceptionUsaSuStatusYMensaje() throws Exception {
        mockMvc.perform(get("/test/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("El email ya está registrado"))
                .andExpect(jsonPath("$.detalles").doesNotExist());
    }

    @Test
    void noEncontradoDa404ConCuerpo() throws Exception {
        mockMvc.perform(get("/test/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Propiedad no encontrada"));
    }

    @Test
    void violacionDeIntegridadDa409SinFiltrarDetalle() throws Exception {
        mockMvc.perform(get("/test/integridad"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("La operación viola una restricción de integridad de datos"));
    }

    @Test
    void errorInesperadoDa500GenericoSinFiltrarDetalle() throws Exception {
        mockMvc.perform(get("/test/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));
    }

    @Test
    void jsonMalFormadoDa400() throws Exception {
        mockMvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("El cuerpo de la solicitud es inválido o está mal formado"));
    }

    @Test
    void parametroDeTipoInvalidoDa400() throws Exception {
        mockMvc.perform(get("/test/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void metodoNoPermitidoDa405() throws Exception {
        mockMvc.perform(post("/test/conflicto"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Método HTTP no permitido"));
    }

    @Test
    void rutaInexistenteDa404() throws Exception {
        mockMvc.perform(get("/no/existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void excepcionesDeSeguridadSeRelanzanParaSpringSecurity() {
        // No deben convertirse en 500: se relanzan para que las resuelva SecurityConfig (403).
        ServletException e = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/test/prohibido")));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
    }
}
