package app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Orígenes desde los que el navegador puede llamar a la API (issue #91).
 *
 * Se configuran con la propiedad cors.allowed-origins (o la variable de entorno
 * CORS_ALLOWED_ORIGINS), separados por coma. Por defecto solo el front local de Vite.
 * Se aceptan comodines para las previews del hosting, por ejemplo:
 *
 *   CORS_ALLOWED_ORIGINS=https://alquileres-r4.vercel.app,https://*.vercel.app
 */
@Configuration
public class CorsConfig {

    private final List<String> origenesPermitidos;

    public CorsConfig(@Value("${cors.allowed-origins:http://localhost:5173}") String origenes) {
        this.origenesPermitidos = Arrays.stream(origenes.split(","))
                .map(String::trim)
                .map(origen -> origen.endsWith("/") ? origen.substring(0, origen.length() - 1) : origen)
                .filter(origen -> !origen.isEmpty())
                .toList();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Patrones y no orígenes fijos: permiten comodines y siguen funcionando con credenciales
        config.setAllowedOriginPatterns(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    List<String> getOrigenesPermitidos() {
        return origenesPermitidos;
    }
}
