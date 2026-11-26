package app.controller;

import app.service.CompartirPropiedadService;
import app.service.CompartirPropiedadService.VistaPrevia;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Issue #151: HTML con Open Graph para la vista previa al compartir. */
class CompartirControllerTest {

    private final CompartirPropiedadService service = mock(CompartirPropiedadService.class);
    private final CompartirController controller = new CompartirController(service);

    private VistaPrevia vista(String titulo) {
        return new VistaPrevia(titulo, "$ 350.000 por mes · Sobremonte 1234",
                "https://alkilea.vercel.app/compartir-alkilea.png", true,
                "https://alkilea.vercel.app/propiedades/7");
    }

    @Test
    void devuelveLasEtiquetasOpenGraphYRedirigeAlFront() {
        when(service.vistaPrevia(7L)).thenReturn(Optional.of(vista("Depto centro")));

        ResponseEntity<String> respuesta = controller.propiedad(7L);
        String html = respuesta.getBody();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertTrue(respuesta.getHeaders().getContentType().toString().startsWith("text/html"));
        assertTrue(html.contains("<meta property=\"og:title\" content=\"Depto centro\">"));
        assertTrue(html.contains("<meta property=\"og:description\" content=\"$ 350.000 por mes · Sobremonte 1234\">"));
        assertTrue(html.contains("<meta property=\"og:image\" content=\"https://alkilea.vercel.app/compartir-alkilea.png\">"));
        assertTrue(html.contains("og:image:width"));
        assertTrue(html.contains("<meta http-equiv=\"refresh\" content=\"0; url=https://alkilea.vercel.app/propiedades/7\">"));
        assertTrue(html.contains("location.replace(\"https://alkilea.vercel.app/propiedades/7\")"));
    }

    @Test
    void escapaLosTextosDelPublicador() {
        when(service.vistaPrevia(7L)).thenReturn(Optional.of(vista("\"><script>alert(1)</script>")));

        String html = controller.propiedad(7L).getBody();

        assertFalse(html.contains("<script>alert(1)</script>"));
        assertTrue(html.contains("&quot;&gt;&lt;script&gt;"));
    }

    @Test
    void siNoExisteDa404PeroIgualMandaAlFront() {
        when(service.vistaPrevia(99L)).thenReturn(Optional.empty());
        when(service.urlDetalle(99L)).thenReturn("https://alkilea.vercel.app/propiedades/99");

        ResponseEntity<String> respuesta = controller.propiedad(99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertFalse(respuesta.getBody().contains("og:title"));
        assertTrue(respuesta.getBody().contains("url=https://alkilea.vercel.app/propiedades/99"));
    }
}
