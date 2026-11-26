package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.CambiarEstadoPropiedadRequest;
import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.service.GestionPropiedadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Editar, marcar como alquilada y eliminar una propiedad publicada. Requieren JWT (caen en
 * anyRequest().authenticated()) y solo los puede usar quien la publicó.
 */
@RestController
@RequestMapping("/propiedades")
public class GestionPropiedadController {

    private final GestionPropiedadService gestionPropiedadService;

    public GestionPropiedadController(GestionPropiedadService gestionPropiedadService) {
        this.gestionPropiedadService = gestionPropiedadService;
    }

    /** Mismo body que publicar: reemplaza todos los datos de la propiedad. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PropiedadDetalleResponse>> editar(@PathVariable Long id,
                                                                        @Valid @RequestBody PropiedadRequest request,
                                                                        Authentication authentication) {
        return ApiResponse.ok("Propiedad actualizada",
                gestionPropiedadService.editar(id, request, authentication.getName()));
    }

    /** {"estado": "ALQUILADA"} o {"estado": "DISPONIBLE"}. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PropiedadDetalleResponse>> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoPropiedadRequest request,
            Authentication authentication) {
        return ApiResponse.ok("Estado de la propiedad actualizado",
                gestionPropiedadService.cambiarEstado(id, request.getEstado(), authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id, Authentication authentication) {
        gestionPropiedadService.eliminar(id, authentication.getName());
        return ApiResponse.ok("Propiedad eliminada", null);
    }
}
