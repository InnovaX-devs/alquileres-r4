package app.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuración de imágenes (prefijo {@code app.imagenes}). Todos los valores tienen default,
 * así que no es obligatorio declararlos en application.properties.
 *
 * @param proveedor          implementación de {@link AlmacenamientoImagenes} a usar ("cloudinary")
 * @param carpetaBase        carpeta raíz dentro del proveedor
 * @param tamanioMaximoBytes tamaño máximo aceptado por imagen
 * @param tiposPermitidos    content types aceptados
 * @param maximoPorPropiedad cantidad máxima de imágenes por propiedad
 */
@ConfigurationProperties(prefix = "app.imagenes")
public record ImagenesProperties(
        String proveedor,
        String carpetaBase,
        Long tamanioMaximoBytes,
        List<String> tiposPermitidos,
        Integer maximoPorPropiedad
) {
    public ImagenesProperties {
        if (proveedor == null || proveedor.isBlank()) proveedor = "cloudinary";
        if (carpetaBase == null || carpetaBase.isBlank()) carpetaBase = "alquileres";
        if (tamanioMaximoBytes == null) tamanioMaximoBytes = 5L * 1024 * 1024;
        if (tiposPermitidos == null || tiposPermitidos.isEmpty())
            tiposPermitidos = List.of("image/jpeg", "image/png", "image/webp");
        if (maximoPorPropiedad == null) maximoPorPropiedad = 20;
    }

    /** Defaults, útil en tests. */
    public static ImagenesProperties porDefecto() {
        return new ImagenesProperties(null, null, null, null, null);
    }
}
