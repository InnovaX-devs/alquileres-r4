package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.FavoritoInmobiliariaResponse;
import app.controller.dto.FavoritosResponse;
import app.controller.dto.FavoritoPropiedadResponse;
import app.model.FavoritoInmobiliaria;
import app.model.FavoritoPropiedad;
import app.service.FavoritoService;
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
    public ResponseEntity<ApiResponse<FavoritosResponse>> listarFavoritos(Authentication authentication) {
        return ApiResponse.ok("Favoritos obtenidos", favoritoService.listarFavoritos(authentication.getName()));
    }

    @PostMapping("/propiedades/{propiedadId}")
    public ResponseEntity<ApiResponse<FavoritoPropiedadResponse>> guardarPropiedadFavorita(
            @PathVariable Long propiedadId,
            Authentication authentication
    ) {
        FavoritoPropiedad favorito = favoritoService.guardarPropiedadFavorita(propiedadId, authentication.getName());
        return ApiResponse.created("Propiedad guardada en favoritos", FavoritoPropiedadResponse.from(favorito));
    }

    @PostMapping("/inmobiliarias/{inmobiliariaId}")
    public ResponseEntity<ApiResponse<FavoritoInmobiliariaResponse>> guardarInmobiliariaFavorita(
            @PathVariable Long inmobiliariaId,
            Authentication authentication
    ) {
        FavoritoInmobiliaria favorito = favoritoService.guardarInmobiliariaFavorita(inmobiliariaId, authentication.getName());
        return ApiResponse.created("Inmobiliaria guardada en favoritos", FavoritoInmobiliariaResponse.from(favorito));
    }
}
