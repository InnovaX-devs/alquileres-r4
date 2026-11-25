package app.controller.dto;

/** Ubicación sugerida para una dirección (issue #140): GET /geocodificacion. */
public record GeocodificacionResponse(double latitud, double longitud, String direccion) {
}
