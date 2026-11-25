package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.GeocodificacionResponse;
import app.geo.GeocodificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/geocodificacion")
public class GeocodificacionController {

    private final GeocodificacionService geocodificacionService;

    public GeocodificacionController(GeocodificacionService geocodificacionService) {
        this.geocodificacionService = geocodificacionService;
    }

    /**
     * Sugerencia de ubicación para el pin del mapa al publicar (issue #140). Requiere sesión.
     * Si no hay una sugerencia confiable (o Georef falla), responde 200 con data: null.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GeocodificacionResponse>> sugerir(
            @RequestParam String calle,
            @RequestParam(required = false) String altura) {
        return geocodificacionService.sugerir(calle, altura)
                .map(sugerencia -> ApiResponse.ok("Ubicación sugerida", sugerencia))
                .orElseGet(() -> ApiResponse.ok("No se encontró la dirección", null));
    }
}
