package app.controller;

import app.service.CompartirPropiedadService;
import app.service.CompartirPropiedadService.VistaPrevia;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;

/**
 * Link para compartir una propiedad (issue #151).
 *
 * Devuelve un HTML mínimo con las etiquetas Open Graph, que es lo que leen WhatsApp, Telegram, etc.
 * para armar la vista previa (no ejecutan JavaScript, así que la SPA no les sirve). A una persona
 * que abre el link, el mismo HTML la manda enseguida al detalle en el front.
 *
 * No es un redirect 302 a propósito: el bot de la vista previa lo seguiría y terminaría en la SPA,
 * sin las etiquetas.
 */
@RestController
@RequestMapping("/compartir")
public class CompartirController {

    private static final MediaType HTML_UTF8 = new MediaType(MediaType.TEXT_HTML, java.nio.charset.StandardCharsets.UTF_8);

    private final CompartirPropiedadService compartirService;

    public CompartirController(CompartirPropiedadService compartirService) {
        this.compartirService = compartirService;
    }

    @GetMapping("/propiedades/{id}")
    public ResponseEntity<String> propiedad(@PathVariable Long id) {
        return compartirService.vistaPrevia(id)
                .map(vista -> ResponseEntity.ok()
                        .contentType(HTML_UTF8)
                        // Unos minutos de caché: si se edita la propiedad, la vista previa se actualiza pronto
                        .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                        .body(paginaConVistaPrevia(vista)))
                // Si no existe se manda igual al front, que muestra su página de "no encontrada"
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(HTML_UTF8)
                        .body(pagina("Propiedad no encontrada", "", compartirService.urlDetalle(id))));
    }

    static String paginaConVistaPrevia(VistaPrevia vista) {
        String titulo = e(vista.titulo());
        String descripcion = e(vista.descripcion());
        StringBuilder meta = new StringBuilder()
                .append("<meta name=\"description\" content=\"").append(descripcion).append("\">\n")
                .append("<meta property=\"og:type\" content=\"website\">\n")
                .append("<meta property=\"og:site_name\" content=\"Alkilea\">\n")
                .append("<meta property=\"og:locale\" content=\"es_AR\">\n")
                .append("<meta property=\"og:title\" content=\"").append(titulo).append("\">\n")
                .append("<meta property=\"og:description\" content=\"").append(descripcion).append("\">\n")
                .append("<meta property=\"og:image\" content=\"").append(e(vista.imagen())).append("\">\n")
                .append("<meta property=\"og:image:alt\" content=\"").append(titulo).append("\">\n");
        if (vista.conMedidas()) {
            meta.append("<meta property=\"og:image:width\" content=\"1200\">\n")
                .append("<meta property=\"og:image:height\" content=\"630\">\n");
        }
        meta.append("<meta name=\"twitter:card\" content=\"summary_large_image\">\n");
        return pagina(vista.titulo(), meta.toString(), vista.urlDestino());
    }

    private static String pagina(String titulo, String meta, String destino) {
        if (destino == null) destino = "/";
        String url = e(destino);
        // El destino va también en un string de JS: se escapa para ese contexto
        String urlJs = destino.replace("\\", "\\\\").replace("\"", "\\\"").replace("<", "\\u003c");
        return "<!DOCTYPE html>\n"
                + "<html lang=\"es-AR\">\n<head>\n"
                + "<meta charset=\"utf-8\">\n"
                + "<title>" + e(titulo) + " | Alkilea</title>\n"
                + meta
                + "<meta http-equiv=\"refresh\" content=\"0; url=" + url + "\">\n"
                + "<script>location.replace(\"" + urlJs + "\")</script>\n"
                + "</head>\n<body>\n"
                + "<p><a href=\"" + url + "\">Ver la propiedad en Alkilea</a></p>\n"
                + "</body>\n</html>\n";
    }

    private static String e(String texto) {
        return HtmlUtils.htmlEscape(texto == null ? "" : texto, "UTF-8");
    }
}
