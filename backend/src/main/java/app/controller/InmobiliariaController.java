package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.InmobiliariaRequest;
import app.controller.dto.InmobiliariaResponse;
import app.model.Inmobiliaria;
import app.service.InmobiliariaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Inmobiliarias (issue #61). Requiere JWT (cae en anyRequest().authenticated()). */
@RestController
@RequestMapping("/inmobiliarias")
public class InmobiliariaController {

    private final InmobiliariaService inmobiliariaService;

    public InmobiliariaController(InmobiliariaService inmobiliariaService) {
        this.inmobiliariaService = inmobiliariaService;
    }

    /** Crea una inmobiliaria y vincula al usuario autenticado a ella. */
    @PostMapping
    public ResponseEntity<ApiResponse<InmobiliariaResponse>> crear(@Valid @RequestBody InmobiliariaRequest request,
                                                                   Authentication authentication) {
        Inmobiliaria inmobiliaria = inmobiliariaService.crearYVincular(request, authentication.getName());
        return ApiResponse.created("Inmobiliaria creada y vinculada", InmobiliariaResponse.from(inmobiliaria));
    }
}
