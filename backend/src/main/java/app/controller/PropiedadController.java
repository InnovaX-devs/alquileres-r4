package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.Propiedad;
import app.service.PropiedadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropiedadDetalleResponse>> getDetalle(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        PropiedadDetalleResponse response = propiedadService.getDetalle(id, email);
        return ApiResponse.ok("Propiedad obtenida", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropiedadDetalleResponse>>> listar() {
        return ApiResponse.ok("Propiedades obtenidas", propiedadService.listar());
    }
}