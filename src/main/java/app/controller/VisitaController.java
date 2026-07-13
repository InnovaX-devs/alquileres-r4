package app.controller;

import app.controller.dto.SolicitudVisitaRequest;
import app.controller.dto.SolicitudVisitaResponse;
import app.model.SolicitudVisita;
import app.service.VisitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visitas")
public class VisitaController {

    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @PostMapping
    public ResponseEntity<SolicitudVisitaResponse> solicitarVisita(
            @RequestBody SolicitudVisitaRequest request,
            Authentication authentication
    ) {
        SolicitudVisita solicitud = visitaService.solicitarVisita(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudVisitaResponse.from(solicitud));
    }
}