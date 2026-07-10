package app.controller;

import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.Propiedad;
import app.service.PropiedadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/propiedades")
public class PropiedadController {

    private final PropiedadService propiedadService;

    public PropiedadController(PropiedadService propiedadService) {
        this.propiedadService = propiedadService;
    }

    @PostMapping
    public ResponseEntity<Propiedad> publicar(@RequestBody PropiedadRequest request,
                                               Principal principal) {
        Propiedad propiedad = propiedadService.publicar(request, principal.getName());
        return ResponseEntity.ok(propiedad);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropiedadDetalleResponse> getDetalle(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        PropiedadDetalleResponse response = propiedadService.getDetalle(id, email);
        return ResponseEntity.ok(response);
    }
}