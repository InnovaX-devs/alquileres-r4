package app.storage.cloudinary;

import app.storage.AlmacenamientoImagenes;
import app.storage.AlmacenamientoImagenesException;
import app.storage.ArchivoImagen;
import app.storage.ImagenAlmacenada;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Implementación de {@link AlmacenamientoImagenes} sobre Cloudinary.
 * Es la que se usa por defecto ({@code app.imagenes.proveedor=cloudinary} o sin definir).
 * Todo el código que conoce la API de Cloudinary vive en este paquete.
 */
@Component
@ConditionalOnProperty(name = "app.imagenes.proveedor", havingValue = "cloudinary", matchIfMissing = true)
public class CloudinaryAlmacenamientoImagenes implements AlmacenamientoImagenes {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryAlmacenamientoImagenes.class);

    private final Cloudinary cloudinary;

    public CloudinaryAlmacenamientoImagenes(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public ImagenAlmacenada subir(ArchivoImagen archivo, String carpeta) {
        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(archivo.contenido(), ObjectUtils.asMap(
                    "folder", carpeta,
                    "resource_type", "image",
                    "use_filename", false,
                    "unique_filename", true,
                    "overwrite", false));

            String url = (String) resultado.get("secure_url");
            String publicId = (String) resultado.get("public_id");
            log.info("Imagen subida a Cloudinary: publicId={}", publicId);
            return new ImagenAlmacenada(url, publicId);
        } catch (IOException | RuntimeException e) {
            throw new AlmacenamientoImagenesException("No se pudo subir la imagen a Cloudinary", e);
        }
    }

    @Override
    public void eliminar(String identificador) {
        try {
            cloudinary.uploader().destroy(identificador, ObjectUtils.asMap("resource_type", "image"));
            log.info("Imagen eliminada de Cloudinary: publicId={}", identificador);
        } catch (IOException | RuntimeException e) {
            throw new AlmacenamientoImagenesException("No se pudo eliminar la imagen de Cloudinary", e);
        }
    }
}
