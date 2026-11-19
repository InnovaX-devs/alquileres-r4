package app.controller;

import app.exception.GlobalExceptionHandler;
import app.model.Alerta;
import app.model.Fuente;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.service.AlertaService;
import app.service.AuthService;
import app.service.PropiedadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #49: los controllers responden con DTOs y no exponen entidades JPA
 * (en particular, el usuario dueño y su hash de contraseña).
 */
class RespuestasDtoTest {

    private PropiedadService propiedadService;
    private AlertaService alertaService;
    private AuthService authService;
    private MockMvc mockMvc;
    private final UsernamePasswordAuthenticationToken principal =
            new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());

    @BeforeEach
    void setUp() {
        propiedadService = mock(PropiedadService.class);
        alertaService = mock(AlertaService.class);
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PropiedadController(propiedadService),
                        new AlertaController(alertaService),
                        new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setEmail("user@test.com");
        usuario.setPasswordHash("$2a$10$hash-secreto");
        return usuario;
    }

    @Test
    void publicar_devuelveDtoSinUsuario() throws Exception {
        Propiedad propiedad = new Propiedad();
        propiedad.setId(7L);
        propiedad.setTitulo("Depto 2 amb");
        propiedad.setPrecio(new BigDecimal("350000"));
        propiedad.setMoneda(Moneda.ARS);
        propiedad.setFuente(Fuente.MANUAL);
        propiedad.setUsuario(usuario());
        when(propiedadService.publicar(any(), anyString())).thenReturn(propiedad);

        String body = """
                {"titulo":"Depto 2 amb","precio":350000,"moneda":"ARS","calle":"Sarmiento","altura":"123"}""";
        mockMvc.perform(post("/propiedades").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.titulo").value("Depto 2 amb"))
                .andExpect(jsonPath("$.moneda").value("ARS"))
                .andExpect(jsonPath("$.fuente").value("MANUAL"))
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.contacto").doesNotExist());
    }

    @Test
    void crearAlerta_devuelveDtoSinUsuario() throws Exception {
        Alerta alerta = new Alerta();
        alerta.setId(3L);
        alerta.setHabitacionesMin(2);
        alerta.setUsuario(usuario());
        when(alertaService.crearAlerta(any(), anyString())).thenReturn(alerta);

        mockMvc.perform(post("/alertas").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"habitacionesMin\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.habitacionesMin").value(2))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$.usuario").doesNotExist());
    }

    @Test
    void register_devuelveMensajeJson() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"email\":\"a@test.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado correctamente"));
    }
}
