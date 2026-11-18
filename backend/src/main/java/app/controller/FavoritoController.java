package app.controller;

import app.controller.dto.FavoritoInmobiliariaResponse;
import app.controller.dto.FavoritosResponse;
import app.controller.dto.FavoritoPropiedadResponse;
import app.model.FavoritoInmobiliaria;
import app.model.FavoritoPropiedad;
import app.service.FavoritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    public ResponseEntity<FavoritosResponse> listarFavoritos(Authentication authentication) {
        return ResponseEntity.ok(favoritoService.listarFavoritos(authentication.getName()));
    }

    @PostMapping("/propiedades/{propiedadId}")
    public ResponseEntity<FavoritoPropiedadResponse> guardarPropiedadFavorita(
            @PathVariable Long propiedadId,
            Authentication authentication
    ) {
        FavoritoPropiedad favorito = favoritoService.guardarPropiedadFavorita(propiedadId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(FavoritoPropiedadResponse.from(favorito));
    }

    @PostMapping("/inmobiliarias/{inmobiliariaId}")
    public ResponseEntity<FavoritoInmobiliariaResponse> guardarInmobiliariaFavorita(
            @PathVariable Long inmobiliariaId,
            Authentication authentication
    ) {
        FavoritoInmobiliaria favorito = favoritoService.guardarInmobiliariaFavorita(inmobiliariaId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(FavoritoInmobiliariaResponse.from(favorito));
    }
}
