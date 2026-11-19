package app.controller;

import app.exception.GlobalExceptionHandler;
import app.model.Alerta;
import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;
import app.service.AlertaService;
import app.service.PropiedadService;
import app.service.VisitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issues #46, #47 y #48: validación de campos obligatorios al publicar propiedad,
 * crear solicitud de visita y crear alerta. Usa los controllers reales con services mockeados.
 */
class ValidacionesRequestTest {

    private PropiedadService propiedadService;
    private VisitaService visitaService;
    private AlertaService alertaService;
    private MockMvc mockMvc;
    // Authentication también es Principal: sirve para los parámetros Principal y Authentication de los controllers
    private final UsernamePasswordAuthenticationToken principal =
            new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());

    @BeforeEach
    void setUp() {
        propiedadService = mock(PropiedadService.class);
        visitaService = mock(VisitaService.class);
        alertaService = mock(AlertaService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PropiedadController(propiedadService),
                        new VisitaController(visitaService),
                        new AlertaController(alertaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── #46 Publicar propiedad ───────────────────────────────────────────────

    @Test
    void publicar_sinCamposObligatorios_devuelve400ConDetallePorCampo() throws Exception {
        mockMvc.perform(post("/propiedades").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles", hasItem("titulo: El título es obligatorio")))
                .andExpect(jsonPath("$.detalles", hasItem("precio: El precio es obligatorio")))
                .andExpect(jsonPath("$.detalles", hasItem("moneda: La moneda es obligatoria")))
                .andExpect(jsonPath("$.detalles", hasItem("calle: La calle es obligatoria")))
                .andExpect(jsonPath("$.detalles", hasItem("altura: La altura es obligatoria")));
        verifyNoInteractions(propiedadService);
    }

    @Test
    void publicar_conTextosEnBlancoYPrecioNoPositivo_devuelve400() throws Exception {
        String body = """
                {"titulo":"   ","precio":0,"moneda":"ARS","calle":"","altura":" "}""";
        mockMvc.perform(post("/propiedades").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("titulo: El título es obligatorio")))
                .andExpect(jsonPath("$.detalles", hasItem("precio: El precio debe ser mayor a 0")))
                .andExpect(jsonPath("$.detalles", hasItem("calle: La calle es obligatoria")))
                .andExpect(jsonPath("$.detalles", hasItem("altura: La altura es obligatoria")));
        verifyNoInteractions(propiedadService);
    }

    @Test
    void publicar_conCamposObligatorios_publica() throws Exception {
        when(propiedadService.publicar(any(), anyString())).thenReturn(new Propiedad());
        String body = """
                {"titulo":"Depto 2 amb","precio":350000,"moneda":"ARS","calle":"Sarmiento","altura":"123"}""";
        mockMvc.perform(post("/propiedades").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(propiedadService).publicar(any(), anyString());
    }

    // ── #47 Solicitud de visita ──────────────────────────────────────────────

    @Test
    void visita_sinFechaPropuesta_devuelve400ConMensajeClaro() throws Exception {
        mockMvc.perform(post("/visitas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"propiedadId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles", hasItem("fechaPropuesta: La fecha propuesta es obligatoria")));
        verifyNoInteractions(visitaService);
    }

    @Test
    void visita_sinPropiedad_devuelve400() throws Exception {
        String fecha = LocalDateTime.now().plusDays(2).withNano(0).toString();
        mockMvc.perform(post("/visitas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fechaPropuesta\":\"" + fecha + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("propiedadId: La propiedad es obligatoria")));
        verifyNoInteractions(visitaService);
    }

    @Test
    void visita_conFechaPasada_devuelve400() throws Exception {
        String fecha = LocalDateTime.now().minusDays(1).withNano(0).toString();
        mockMvc.perform(post("/visitas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propiedadId\":1,\"fechaPropuesta\":\"" + fecha + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("fechaPropuesta: La fecha propuesta debe ser futura")));
        verifyNoInteractions(visitaService);
    }

    @Test
    void visita_valida_creaSolicitud() throws Exception {
        SolicitudVisita s = new SolicitudVisita();
        s.setUsuario(new Usuario());
        s.setPropiedad(new Propiedad());
        when(visitaService.solicitarVisita(any(), anyString())).thenReturn(s);
        String fecha = LocalDateTime.now().plusDays(2).withNano(0).toString();
        mockMvc.perform(post("/visitas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propiedadId\":1,\"fechaPropuesta\":\"" + fecha + "\"}"))
                .andExpect(status().isCreated());
        verify(visitaService).solicitarVisita(any(), anyString());
    }

    // ── #48 Alertas ──────────────────────────────────────────────────────────

    @Test
    void alerta_sinCriterios_devuelve400() throws Exception {
        mockMvc.perform(post("/alertas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles", hasItem(
                        "criterios: Debe indicar al menos un criterio: precio mínimo, precio máximo, habitaciones mínimas o calle")));
        verifyNoInteractions(alertaService);
    }

    @Test
    void alerta_soloConMonedaOCalleEnBlanco_devuelve400() throws Exception {
        mockMvc.perform(post("/alertas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"moneda\":\"USD\",\"calle\":\"  \"}"))
                .andExpect(status().isBadRequest());
        verify(alertaService, never()).crearAlerta(any(), anyString());
    }

    @Test
    void alerta_conPrecioSinMoneda_devuelve400() throws Exception {
        mockMvc.perform(post("/alertas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precioMax\":500000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem(
                        "monedaConPrecio: La moneda es obligatoria cuando se indica un precio mínimo o máximo")));
    }

    @Test
    void alerta_conRangoInvertido_devuelve400() throws Exception {
        mockMvc.perform(post("/alertas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precioMin\":600000,\"precioMax\":500000,\"moneda\":\"ARS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem(
                        "rangoPrecioValido: El precio mínimo no puede ser mayor al precio máximo")));
    }

    @Test
    void alerta_conUnCriterio_seCrea() throws Exception {
        when(alertaService.crearAlerta(any(), anyString())).thenReturn(new Alerta());
        mockMvc.perform(post("/alertas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"habitacionesMin\":2}"))
                .andExpect(status().isOk());
        verify(alertaService).crearAlerta(any(), anyString());
    }

    @Test
    void alerta_conPrecioYMoneda_seCrea() throws Exception {
        when(alertaService.crearAlerta(any(), anyString())).thenReturn(new Alerta());
        mockMvc.perform(post("/alertas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precioMin\":300000,\"precioMax\":500000,\"moneda\":\"ARS\"}"))
                .andExpect(status().isOk());
    }
}
