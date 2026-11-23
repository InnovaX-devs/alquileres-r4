package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.CambiarEstadoVisitaRequest;
import app.controller.dto.SolicitudVisitaRecibidaResponse;
import app.controller.dto.SolicitudVisitaRequest;
import app.controller.dto.SolicitudVisitaResponse;
import app.model.SolicitudVisita;
import app.service.VisitaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visitas")
public class VisitaController {

    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SolicitudVisitaResponse>> solicitarVisita(
            @Valid @RequestBody SolicitudVisitaRequest request,
            Authentication authentication
    ) {
        SolicitudVisita solicitud = visitaService.solicitarVisita(request, authentication.getName());
        return ApiResponse.created("Solicitud de visita creada", SolicitudVisitaResponse.from(solicitud));
    }

    /** Solicitudes de visita realizadas por el usuario autenticado (issue #56). */
    @GetMapping("/mias")
    public ResponseEntity<ApiResponse<List<SolicitudVisitaResponse>>> listarMias(Authentication authentication) {
        List<SolicitudVisitaResponse> mias = visitaService.listarMias(authentication.getName())
                .stream()
                .map(SolicitudVisitaResponse::from)
                .toList();
        return ApiResponse.ok("Solicitudes obtenidas", mias);
    }

    /** Solicitudes de visita recibidas sobre las propiedades del publicador autenticado (issue #57). */
    @GetMapping("/recibidas")
    public ResponseEntity<ApiResponse<List<SolicitudVisitaRecibidaResponse>>> listarRecibidas(
            Authentication authentication
    ) {
        List<SolicitudVisitaRecibidaResponse> recibidas = visitaService.listarRecibidas(authentication.getName())
                .stream()
                .map(SolicitudVisitaRecibidaResponse::from)
                .toList();
        return ApiResponse.ok("Solicitudes recibidas obtenidas", recibidas);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<SolicitudVisitaResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambiarEstadoVisitaRequest request,
            Authentication authentication
    ) {
        SolicitudVisita solicitud = visitaService.cambiarEstado(id, request, authentication.getName());
        return ApiResponse.ok("Estado de la visita actualizado", SolicitudVisitaResponse.from(solicitud));
    }
}