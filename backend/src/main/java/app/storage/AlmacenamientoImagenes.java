package app.storage;

/**
 * Abstracción del lugar donde se guardan los archivos de imagen (issue #59).
 *
 * El resto de la aplicación (servicios, controllers) depende solo de esta interfaz, nunca de un
 * proveedor concreto. Para cambiar la forma en que se tratan las imágenes (otro proveedor como S3,
 * disco local, transformaciones distintas, etc.) alcanza con:
 *   1. Crear una nueva implementación de esta interfaz anotada con
 *      {@code @ConditionalOnProperty(name = "app.imagenes.proveedor", havingValue = "<nombre>")}.
 *   2. Cambiar {@code app.imagenes.proveedor} en application.properties.
 * No hace falta tocar ImagenPropiedadService ni los controllers.
 */
public interface AlmacenamientoImagenes {

    /**
     * Sube la imagen y devuelve su URL pública junto con el identificador que usa el proveedor.
     *
     * @param archivo imagen ya validada (tipo y tamaño) por la capa de servicio
     * @param carpeta ruta lógica donde agrupar la imagen (p. ej. "propiedades/15")
     * @throws AlmacenamientoImagenesException si el proveedor falla
     */
    ImagenAlmacenada subir(ArchivoImagen archivo, String carpeta);

    /**
     * Elimina una imagen previamente subida. Se usa, por ejemplo, para compensar cuando la imagen
     * se subió pero no se pudo guardar en la base.
     *
     * @param identificador el valor devuelto en {@link ImagenAlmacenada#identificador()}
     * @throws AlmacenamientoImagenesException si el proveedor falla
     */
    void eliminar(String identificador);
}
