package app.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Registra {@link ImagenesProperties} (prefijo app.imagenes). */
@Configuration
@EnableConfigurationProperties(ImagenesProperties.class)
public class ImagenesConfig {
}
