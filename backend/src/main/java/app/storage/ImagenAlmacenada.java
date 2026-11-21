package app.storage;

/**
 * Resultado de subir una imagen.
 *
 * @param url           URL pública (https) para mostrar la imagen
 * @param identificador id de la imagen dentro del proveedor (en Cloudinary, el public_id);
 *                      se persiste para poder borrarla o reemplazarla más adelante
 */
public record ImagenAlmacenada(String url, String identificador) {
}
