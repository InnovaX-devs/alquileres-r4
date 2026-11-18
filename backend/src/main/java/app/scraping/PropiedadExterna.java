package app.scraping;

/**
 * Datos crudos de una propiedad tal como se leen de una fuente externa,
 * antes de mapearlos al modelo interno.
 */
public record PropiedadExterna(
        String titulo,
        String descripcion,
        String precio,
        String direccion,
        Integer habitaciones,
        Integer metrosCuadrados,
        String url
) {}
