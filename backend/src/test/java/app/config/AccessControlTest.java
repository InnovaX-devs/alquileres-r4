package app.config;

import app.model.Rol;
import app.model.Usuario;
import app.service.AlertaService;
import app.service.AuthService;
import app.service.FavoritoService;
import app.service.ImagenPropiedadService;
import app.service.InmobiliariaService;
import app.service.PropiedadService;
import app.service.UsuarioService;
import app.service.VisitaService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida las reglas de acceso (issue #44): endpoints públicos vs privados según JWT.
 * Solo levanta la capa web + seguridad; los services van mockeados (no necesita DB).
 */
@WebMvcTest
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, CorsConfig.class})
@TestPropertySource(properties = {
        "jwt.secret=clave-de-test-con-mas-de-32-bytes-para-hmac-sha256",
        "jwt.expiration=3600000"
})
class AccessControlTest {

    private static final String SECRET = "clave-de-test-con-mas-de-32-bytes-para-hmac-sha256";

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    @MockBean AlertaService alertaService;
    @MockBean AuthService authService;
    @MockBean FavoritoService favoritoService;
    @MockBean ImagenPropiedadService imagenPropiedadService;
    @MockBean InmobiliariaService inmobiliariaService;
    @MockBean PropiedadService propiedadService;
    @MockBean UsuarioService usuarioService;
    @MockBean VisitaService visitaService;

    private MockHttpServletRequestBuilder req(String metodo, String path) {
        return request(HttpMethod.valueOf(metodo), path)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}");
    }

    /** Status devuelto por la capa de seguridad. */
    private static final int PASO_LA_SEGURIDAD = -1;

    /**
     * Ejecuta el request y devuelve el status. Si el request atraviesa la seguridad y el controller
     * falla porque el service mockeado devuelve null (ServletException), significa que la seguridad
     * lo dejó pasar: se devuelve PASO_LA_SEGURIDAD (distinto de 401 y 403).
     */
    private int statusTrasSeguridad(MockHttpServletRequestBuilder builder) throws Exception {
        try {
            return mockMvc.perform(builder).andReturn().getResponse().getStatus();
        } catch (ServletException e) {
            return PASO_LA_SEGURIDAD;
        }
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // ── Endpoints públicos: accesibles sin token ─────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "GET,/propiedades",
            "GET,/propiedades/1",
            "POST,/auth/login",
            "POST,/auth/register"
    })
    void publicosNoRequierenToken(String metodo, String path) throws Exception {
        int status = statusTrasSeguridad(req(metodo, path));
        assertNotEquals(401, status, metodo + " " + path + " debería ser público");
        assertNotEquals(403, status, metodo + " " + path + " debería ser público");
    }

    @Test
    void publicoIgnoraTokenInvalido() throws Exception {
        mockMvc.perform(req("GET", "/propiedades").header("Authorization", bearer("basura")))
                .andExpect(status().isOk());
    }

    @Test
    void errorEndpointEsPublico() throws Exception {
        // Si /error exigiera token, cualquier 403/404/409 de un controller se vería como 401.
        int status = statusTrasSeguridad(req("GET", "/error"));
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

    // ── Endpoints privados: sin token -> 401 ─────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "POST,/propiedades",
            "POST,/propiedades/1/imagenes",
            "POST,/inmobiliarias",
            "POST,/alertas",
            "GET,/favoritos",
            "POST,/favoritos/propiedades/1",
            "POST,/favoritos/inmobiliarias/1",
            "PATCH,/usuarios/perfil",
            "GET,/usuarios/me",
            "GET,/propiedades/mias",
            "POST,/visitas",
            "GET,/visitas/recibidas",
            "PATCH,/visitas/1/estado",
            "GET,/alertas",
            "PATCH,/alertas/1",
            "DELETE,/alertas/1",
            "POST,/fuentes-externas/1/importar"
    })
    void privadosSinTokenDan401(String metodo, String path) throws Exception {
        mockMvc.perform(req(metodo, path))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ── Endpoints privados: token inválido -> 401 ────────────────────────────

    @Test
    void tokenMalformadoDa401() throws Exception {
        mockMvc.perform(req("GET", "/favoritos").header("Authorization", bearer("esto.no.es.un.jwt")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenVencidoDa401() throws Exception {
        String vencido = Jwts.builder()
                .subject("user@test.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7_200_000))
                .expiration(new Date(System.currentTimeMillis() - 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        mockMvc.perform(req("GET", "/favoritos").header("Authorization", bearer(vencido)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFirmadoConOtraClaveDa401() throws Exception {
        String ajeno = Jwts.builder()
                .subject("user@test.com")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor("otra-clave-distinta-de-mas-de-32-bytes-123456".getBytes()))
                .compact();

        mockMvc.perform(req("GET", "/favoritos").header("Authorization", bearer(ajeno)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void esquemaDistintoDeBearerDa401() throws Exception {
        String token = jwtUtil.generateToken("user@test.com", Rol.USER);

        mockMvc.perform(req("GET", "/favoritos").header("Authorization", "Basic " + token))
                .andExpect(status().isUnauthorized());
    }

    // ── Endpoints privados: token válido -> acceso ───────────────────────────

    @ParameterizedTest
    @CsvSource({
            "POST,/propiedades",
            "POST,/propiedades/1/imagenes",
            "POST,/inmobiliarias",
            "POST,/alertas",
            "GET,/favoritos",
            "POST,/visitas",
            "GET,/visitas/recibidas",
            "GET,/alertas",
            "PATCH,/usuarios/perfil",
            "GET,/usuarios/me",
            "GET,/propiedades/mias"
    })
    void privadosConTokenValidoNoDan401ni403(String metodo, String path) throws Exception {
        String token = jwtUtil.generateToken("user@test.com", Rol.USER);

        int status = statusTrasSeguridad(req(metodo, path).header("Authorization", bearer(token)));

        assertNotEquals(401, status, metodo + " " + path + " con token válido");
        assertNotEquals(403, status, metodo + " " + path + " con token válido");
    }

    // ── GET /usuarios/me (issue #54) ─────────────────────────────────────────

    @Test
    void usuariosMeConTokenDevuelveElUsuarioSinPassword() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNombre("Ana");
        usuario.setEmail("user@test.com");
        usuario.setPasswordHash("$2a$10$hash-secreto");
        usuario.setRol(Rol.USER);
        when(usuarioService.obtenerPorEmail("user@test.com")).thenReturn(usuario);
        String token = jwtUtil.generateToken("user@test.com", Rol.USER);

        mockMvc.perform(get("/usuarios/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.nombre").value("Ana"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.verificado").value(false))
                .andExpect(jsonPath("$.data.rol").value("USER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    // ── GET /propiedades/mias (issue #55) ────────────────────────────────────

    @Test
    void propiedadesMiasNoSeConfundeConElDetalle() throws Exception {
        when(propiedadService.listarMias("user@test.com")).thenReturn(List.of());
        String token = jwtUtil.generateToken("user@test.com", Rol.USER);

        mockMvc.perform(get("/propiedades/mias").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(propiedadService).listarMias("user@test.com");
        verify(propiedadService, never()).getDetalle(any(), any());
    }

    // ── Roles: verificar inmobiliarias solo ADMIN (issue #61) ────────────────

    @Test
    void verificarInmobiliariaComoUserDa403() throws Exception {
        String token = jwtUtil.generateToken("user@test.com", Rol.USER);

        mockMvc.perform(req("PATCH", "/inmobiliarias/1/verificacion").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void verificarInmobiliariaComoAdminTieneAcceso() throws Exception {
        String token = jwtUtil.generateToken("admin@test.com", Rol.ADMIN);

        int status = statusTrasSeguridad(req("PATCH", "/inmobiliarias/1/verificacion")
                .header("Authorization", bearer(token)));

        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

    // ── Roles: /fuentes-externas/** solo para ADMIN ──────────────────────────

    @Test
    void importarComoUserDa403() throws Exception {
        String token = jwtUtil.generateToken("user@test.com", Rol.USER);

        mockMvc.perform(req("POST", "/fuentes-externas/1/importar").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void importarComoAdminTieneAcceso() throws Exception {
        String token = jwtUtil.generateToken("admin@test.com", Rol.ADMIN);

        int status = statusTrasSeguridad(req("POST", "/fuentes-externas/1/importar").header("Authorization", bearer(token)));

        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

    @Test
    void tokenSinClaimDeRolSeTrataComoUser() throws Exception {
        String sinRol = Jwts.builder()
                .subject("viejo@test.com")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        mockMvc.perform(req("POST", "/fuentes-externas/1/importar").header("Authorization", bearer(sinRol)))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenConRolDesconocidoSeTrataComoUser() throws Exception {
        String rolRaro = Jwts.builder()
                .subject("raro@test.com")
                .claim("rol", "SUPERADMIN")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        mockMvc.perform(req("POST", "/fuentes-externas/1/importar").header("Authorization", bearer(rolRaro)))
                .andExpect(status().isForbidden());
    }

    // ── CORS: el preflight no lleva token y debe resolverse sin 401 ──────────

    @ParameterizedTest
    @CsvSource({
            "PATCH,/usuarios/perfil",
            "PATCH,/visitas/1/estado",
            "GET,/favoritos",
            "POST,/propiedades"
    })
    void preflightSinTokenSeResuelveConCors(String metodo, String path) throws Exception {
        mockMvc.perform(options(path)
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", metodo)
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void preflightDeOrigenNoPermitidoSeRechaza() throws Exception {
        mockMvc.perform(options("/favoritos")
                        .header("Origin", "http://sitio-malicioso.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
