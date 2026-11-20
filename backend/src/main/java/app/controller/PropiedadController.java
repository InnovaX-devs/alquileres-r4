package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.Moneda;
import app.model.Propiedad;
import app.service.PropiedadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/propiedades")
public class PropiedadController {

    private final PropiedadService propiedadService;

    public PropiedadController(PropiedadService propiedadService) {
        this.propiedadService = propiedadService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PropiedadDetalleResponse>> publicar(@Valid @RequestBody PropiedadRequest request,
                                               Principal principal) {
        Propiedad propiedad = propiedadService.publicar(request, principal.getName());
        return ApiResponse.created("Propiedad publicada", PropiedadDetalleResponse.from(propiedad, null));
    }

    /** Propiedades publicadas por el usuario autenticado o su inmobiliaria (issue #55). */
    @GetMapping("/mias")
    public ResponseEntity<ApiResponse<List<PropiedadDetalleResponse>>> listarMias(Authentication authentication) {
        return ApiResponse.ok("Propiedades obtenidas", propiedadService.listarMias(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropiedadDetalleResponse>> getDetalle(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        PropiedadDetalleResponse response = propiedadService.getDetalle(id, email);
        return ApiResponse.ok("Propiedad obtenida", response);
    }

    /** Listado público; todos los filtros son opcionales y combinables (issue #58). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PropiedadDetalleResponse>>> listar(
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Moneda moneda,
            @RequestParam(required = false) Integer habitacionesMin,
            @RequestParam(required = false) String calle
    ) {
        return ApiResponse.ok("Propiedades obtenidas",
                propiedadService.listar(precioMin, precioMax, moneda, habitacionesMin, calle));
    }
}