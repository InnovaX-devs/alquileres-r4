package app.storage;

/** Error del proveedor de almacenamiento de imágenes (red, credenciales, cuota, etc.). */
public class AlmacenamientoImagenesException extends RuntimeException {

    public AlmacenamientoImagenesException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
