package app.controller;

import app.controller.dto.AlertaRequest;
import app.model.Alerta;
import app.service.AlertaService;
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
    public ResponseEntity<Alerta> crearAlerta(@RequestBody AlertaRequest request,
                                               Principal principal) {
        Alerta alerta = alertaService.crearAlerta(request, principal.getName());
        return ResponseEntity.ok(alerta);
    }
}