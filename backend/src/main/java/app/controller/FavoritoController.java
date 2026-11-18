package app.controller;

import app.controller.dto.FavoritoPropiedadResponse;
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

    @PostMapping("/propiedades/{propiedadId}")
    public ResponseEntity<FavoritoPropiedadResponse> guardarPropiedadFavorita(
            @PathVariable Long propiedadId,
            Authentication authentication
    ) {
        FavoritoPropiedad favorito = favoritoService.guardarPropiedadFavorita(propiedadId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(FavoritoPropiedadResponse.from(favorito));
    }
}
