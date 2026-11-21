package app.controller.dto;

import app.model.ImagenPropiedad;

/** Respuesta de POST /propiedades/{id}/imagenes (issue #59). */
public record ImagenPropiedadResponse(Long id, String url, Integer orden) {

    public static ImagenPropiedadResponse from(ImagenPropiedad imagen) {
        return new ImagenPropiedadResponse(imagen.getId(), imagen.getUrl(), imagen.getOrden());
    }
}
