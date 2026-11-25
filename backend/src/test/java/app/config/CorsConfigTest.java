package app.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Orígenes de CORS configurables para producción (issue #91). */
class CorsConfigTest {

    private static CorsConfiguration configuracion(String origenes) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/propiedades");
        return new CorsConfig(origenes).corsConfigurationSource().getCorsConfiguration(request);
    }

    @Test
    void porDefectoSoloElFrontLocal() {
        CorsConfig config = new CorsConfig("http://localhost:5173");
        assertEquals(List.of("http://localhost:5173"), config.getOrigenesPermitidos());
    }

    @Test
    void leeVariosOrigenesSeparadosPorComaSinEspaciosNiBarraFinal() {
        CorsConfig config = new CorsConfig(" https://alquileres-r4.vercel.app/ , http://localhost:5173,, ");
        assertEquals(List.of("https://alquileres-r4.vercel.app", "http://localhost:5173"), config.getOrigenesPermitidos());
    }

    @Test
    void aceptaElDominioDeProduccionYRechazaOtros() {
        CorsConfiguration cors = configuracion("https://alquileres-r4.vercel.app,http://localhost:5173");

        assertEquals("https://alquileres-r4.vercel.app", cors.checkOrigin("https://alquileres-r4.vercel.app"));
        assertEquals("http://localhost:5173", cors.checkOrigin("http://localhost:5173"));
        assertNull(cors.checkOrigin("https://sitio-malicioso.com"));
    }

    @Test
    void aceptaComodinesParaLasPreviews() {
        CorsConfiguration cors = configuracion("https://*.vercel.app");

        assertEquals("https://alquileres-r4-git-elias.vercel.app",
                cors.checkOrigin("https://alquileres-r4-git-elias.vercel.app"));
        assertNull(cors.checkOrigin("https://vercel.app.sitio-malicioso.com"));
    }

    @Test
    void permitePatchYCredenciales() {
        CorsConfiguration cors = configuracion("http://localhost:5173");

        assertTrue(cors.getAllowedMethods().contains("PATCH"));
        assertTrue(cors.getAllowCredentials());
    }
}
