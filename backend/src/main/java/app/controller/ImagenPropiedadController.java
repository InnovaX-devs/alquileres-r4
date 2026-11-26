package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.ImagenPropiedadResponse;
import app.model.ImagenPropiedad;
import app.service.ImagenPropiedadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Imágenes de una propiedad (issue #59). Requiere JWT (cae en anyRequest().authenticated()). */
@RestController
@RequestMapping("/propiedades/{id}/imagenes")
public class ImagenPropiedadController {

    private final ImagenPropiedadService imagenPropiedadService;

    public ImagenPropiedadController(ImagenPropiedadService imagenPropiedadService) {
        this.imagenPropiedadService = imagenPropiedadService;
    }

    /** multipart/form-data con el archivo en el campo "imagen". */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImagenPropiedadResponse>> subir(@PathVariable Long id,
                                                                      @RequestParam("imagen") MultipartFile imagen,
                                                                      Authentication authentication) {
        ImagenPropiedad guardada = imagenPropiedadService.subir(id, imagen, authentication.getName());
        return ApiResponse.created("Imagen subida", ImagenPropiedadResponse.from(guardada));
    }

    /** Quita una foto. Solo quien publicó la propiedad. */
    @DeleteMapping("/{imagenId}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id,
                                                      @PathVariable Long imagenId,
                                                      Authentication authentication) {
        imagenPropiedadService.eliminar(id, imagenId, authentication.getName());
        return ApiResponse.ok("Imagen eliminada", null);
    }
}
