package app.controller;

import app.exception.GlobalExceptionHandler;
import app.model.Inmobiliaria;
import app.service.InmobiliariaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Issue #61: POST /inmobiliarias (validaciones y formato de respuesta). */
class InmobiliariaControllerTest {

    private InmobiliariaService service;
    private MockMvc mockMvc;
    private final UsernamePasswordAuthenticationToken principal =
            new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());

    @BeforeEach
    void setUp() {
        service = mock(InmobiliariaService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new InmobiliariaController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void creaYDevuelve201ConLaInmobiliaria() throws Exception {
        Inmobiliaria inmobiliaria = new Inmobiliaria();
        inmobiliaria.setId(7L);
        inmobiliaria.setNombre("Inmo Centro");
        inmobiliaria.setEmail("contacto@inmo.com");
        when(service.crearYVincular(any(), eq("user@test.com"))).thenReturn(inmobiliaria);

        mockMvc.perform(post("/inmobiliarias").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Inmo Centro\",\"email\":\"contacto@inmo.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.nombre").value("Inmo Centro"))
                .andExpect(jsonPath("$.data.verificada").value(false));
    }

    @Test
    void faltanNombreYEmailDevuelve400() throws Exception {
        mockMvc.perform(post("/inmobiliarias").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.length()").value(2));

        verifyNoInteractions(service);
    }

    @Test
    void emailInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/inmobiliarias").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Inmo\",\"email\":\"no-es-email\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
