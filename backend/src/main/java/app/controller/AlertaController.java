package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.AlertaRequest;
import app.controller.dto.AlertaResponse;
import app.model.Alerta;
import app.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

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
}