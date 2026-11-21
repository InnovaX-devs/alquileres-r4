package app.storage;

/**
 * Imagen a subir, independiente de cómo llegó (multipart, URL externa, test, etc.).
 * Así las implementaciones de {@link AlmacenamientoImagenes} no dependen de clases web de Spring.
 */
public record ArchivoImagen(byte[] contenido, String nombreOriginal, String contentType) {
}
