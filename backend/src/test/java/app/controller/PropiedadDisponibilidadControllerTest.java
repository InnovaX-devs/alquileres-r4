package app.controller;

import app.controller.dto.PropiedadRequest;
import app.exception.GlobalExceptionHandler;
import app.model.Propiedad;
import app.service.PropiedadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Issue #154: el filtro disponibleAntesDe de GET /propiedades y el campo disponibleDesde al publicar. */
class PropiedadDisponibilidadControllerTest {

    private PropiedadService propiedadService;
    private MockMvc mockMvc;
    private final UsernamePasswordAuthenticationToken principal =
            new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());

    @BeforeEach
    void setUp() {
        propiedadService = mock(PropiedadService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PropiedadController(propiedadService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void elFiltroLlegaAlServiceComoFecha() throws Exception {
        when(propiedadService.listar(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/propiedades").param("disponibleAntesDe", "2026-03-10"))
                .andExpect(status().isOk());

        verify(propiedadService).listar(null, null, null, null, null, LocalDate.of(2026, 3, 10));
    }

    @Test
    void sinElFiltroLlegaNull() throws Exception {
        when(propiedadService.listar(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/propiedades")).andExpect(status().isOk());

        verify(propiedadService).listar(null, null, null, null, null, null);
    }

    @Test
    void unaFechaConFormatoInvalidoEnElFiltroDa400() throws Exception {
        mockMvc.perform(get("/propiedades").param("disponibleAntesDe", "ayer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void publicarRecibeLaFechaDelJson() throws Exception {
        when(propiedadService.publicar(any(PropiedadRequest.class), anyString())).thenReturn(new Propiedad());
        ArgumentCaptor<PropiedadRequest> captor = ArgumentCaptor.forClass(PropiedadRequest.class);

        mockMvc.perform(post("/propiedades")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Depto","precio":350000,"moneda":"ARS","calle":"Sobremonte",
                                 "altura":"845","disponibleDesde":"2026-03-10"}"""))
                .andExpect(status().isCreated());

        verify(propiedadService).publicar(captor.capture(), anyString());
        assertEquals(LocalDate.of(2026, 3, 10), captor.getValue().getDisponibleDesde());
    }

    @Test
    void unaFechaInvalidaEnElBodyDa400() throws Exception {
        mockMvc.perform(post("/propiedades")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Depto","precio":350000,"moneda":"ARS","calle":"Sobremonte",
                                 "altura":"845","disponibleDesde":"2026-13-45"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
