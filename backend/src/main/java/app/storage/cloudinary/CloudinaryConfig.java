package app.storage.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente de Cloudinary. Solo se crea cuando el proveedor de imágenes es Cloudinary.
 * Las credenciales vienen de application.properties (cloudinary.*), que a su vez puede leerlas
 * de variables de entorno.
 */
@Configuration
@ConditionalOnProperty(name = "app.imagenes.proveedor", havingValue = "cloudinary", matchIfMissing = true)
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.cloud-name:}") String cloudName,
                                 @Value("${cloudinary.api-key:}") String apiKey,
                                 @Value("${cloudinary.api-secret:}") String apiSecret) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            // No se corta el arranque (para poder levantar la app / tests sin credenciales),
            // pero la subida de imágenes va a fallar hasta configurarlas.
            log.warn("Credenciales de Cloudinary incompletas: la subida de imágenes no va a funcionar");
        }
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }
}
