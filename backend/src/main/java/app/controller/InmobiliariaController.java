package app.controller;

import app.controller.dto.ActualizarInmobiliariaRequest;
import app.controller.dto.AgregarMiembroRequest;
import app.controller.dto.ApiResponse;
import app.controller.dto.CambiarVerificacionRequest;
import app.controller.dto.InmobiliariaRequest;
import app.controller.dto.InmobiliariaResponse;
import app.controller.dto.MiembroInmobiliariaResponse;
import app.service.InmobiliariaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inmobiliarias y sus miembros (issue #61). Todos requieren JWT;
 * PATCH /{id}/verificacion además requiere rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/inmobiliarias")
public class InmobiliariaController {

    private final InmobiliariaService inmobiliariaService;

    public InmobiliariaController(InmobiliariaService inmobiliariaService) {
        this.inmobiliariaService = inmobiliariaService;
    }

    /** Crea una inmobiliaria; el usuario autenticado queda como su administrador. */
    @PostMapping
    public ResponseEntity<ApiResponse<InmobiliariaResponse>> crear(@Valid @RequestBody InmobiliariaRequest request,
                                                                   Authentication authentication) {
        var inmobiliaria = inmobiliariaService.crearYVincular(request, authentication.getName());
        return ApiResponse.created("Inmobiliaria creada", InmobiliariaResponse.from(inmobiliaria));
    }

    /** Edita los datos de la inmobiliaria (solo su administrador). */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<InmobiliariaResponse>> actualizar(@PathVariable Long id,
                                                                        @Valid @RequestBody ActualizarInmobiliariaRequest request,
                                                                        Authentication authentication) {
        var inmobiliaria = inmobiliariaService.actualizar(id, request, authentication.getName());
        return ApiResponse.ok("Inmobiliaria actualizada", InmobiliariaResponse.from(inmobiliaria));
    }

    /** Miembros de la inmobiliaria (visible para cualquier miembro). */
    @GetMapping("/{id}/miembros")
    public ResponseEntity<ApiResponse<List<MiembroInmobiliariaResponse>>> listarMiembros(@PathVariable Long id,
                                                                                         Authentication authentication) {
        var miembros = inmobiliariaService.listarMiembros(id, authentication.getName()).stream()
                .map(MiembroInmobiliariaResponse::from)
                .toList();
        return ApiResponse.ok("Miembros obtenidos", miembros);
    }

    /** Vincula como agente a un usuario registrado (solo el administrador). */
    @PostMapping("/{id}/miembros")
    public ResponseEntity<ApiResponse<MiembroInmobiliariaResponse>> agregarMiembro(@PathVariable Long id,
                                                                                   @Valid @RequestBody AgregarMiembroRequest request,
                                                                                   Authentication authentication) {
        var miembro = inmobiliariaService.agregarMiembro(id, request.getEmail(), authentication.getName());
        return ApiResponse.created("Miembro agregado", MiembroInmobiliariaResponse.from(miembro));
    }

    /** Desvincula a un miembro (solo el administrador). */
    @DeleteMapping("/{id}/miembros/{usuarioId}")
    public ResponseEntity<ApiResponse<Void>> quitarMiembro(@PathVariable Long id,
                                                           @PathVariable Long usuarioId,
                                                           Authentication authentication) {
        inmobiliariaService.quitarMiembro(id, usuarioId, authentication.getName());
        return ApiResponse.ok("Miembro quitado", null);
    }

    /** Verifica o quita la verificación de la inmobiliaria (solo ADMIN de la plataforma). */
    @PatchMapping("/{id}/verificacion")
    public ResponseEntity<ApiResponse<InmobiliariaResponse>> cambiarVerificacion(@PathVariable Long id,
                                                                                 @Valid @RequestBody CambiarVerificacionRequest request) {
        var inmobiliaria = inmobiliariaService.cambiarVerificacion(id, request.getVerificada());
        return ApiResponse.ok(request.getVerificada() ? "Inmobiliaria verificada" : "Verificación quitada",
                InmobiliariaResponse.from(inmobiliaria));
    }
}
