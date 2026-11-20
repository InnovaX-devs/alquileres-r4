package app.controller;

import app.controller.dto.AlertaRequest;
import app.controller.dto.AlertaResponse;
import app.controller.dto.ApiResponse;
import app.controller.dto.CambiarEstadoAlertaRequest;
import app.model.Alerta;
import app.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlertaResponse>> crearAlerta(@Valid @RequestBody AlertaRequest request,
                                               Principal principal) {
        Alerta alerta = alertaService.crearAlerta(request, principal.getName());
        return ApiResponse.created("Alerta creada", AlertaResponse.from(alerta));
    }

    /** Alertas del usuario autenticado (issue #60). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertaResponse>>> listarAlertas(Principal principal) {
        List<AlertaResponse> alertas = alertaService.listarAlertas(principal.getName())
                .stream()
                .map(AlertaResponse::from)
                .toList();
        return ApiResponse.ok("Alertas obtenidas", alertas);
    }

    /** Activa o desactiva una alerta propia (issue #60). */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertaResponse>> cambiarEstado(@PathVariable Long id,
                                                                    @Valid @RequestBody CambiarEstadoAlertaRequest request,
                                                                    Principal principal) {
        Alerta alerta = alertaService.cambiarEstado(id, request.getActiva(), principal.getName());
        return ApiResponse.ok("Alerta actualizada", AlertaResponse.from(alerta));
    }

    /** Elimina una alerta propia (issue #60). */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarAlerta(@PathVariable Long id, Principal principal) {
        alertaService.eliminarAlerta(id, principal.getName());
        return ApiResponse.ok("Alerta eliminada", null);
    }
}
