package app.geo;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Cliente de la API Georef de datos.gob.ar (issue #140): normaliza una dirección de Río Cuarto
 * y devuelve sus coordenadas.
 *
 * Consulta /direcciones?direccion=...&localidad_censal=14098230&max=1, con un timeout corto para
 * que una demora de Georef no trabe el formulario de publicar.
 */
@Component
public class GeorefClient {

    /** Dirección que encontró Georef: nombre oficial de la calle, coordenadas y texto completo. */
    public record DireccionGeoref(String calle, double latitud, double longitud, String nomenclatura) {
    }

    private final RestClient restClient;

    public GeorefClient(@Value("${georef.url:https://apis.datos.gob.ar/georef/api/v2.0}") String baseUrl,
                        @Value("${georef.timeout-ms:3000}") int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Busca una dirección ("Sobremonte 845") en Río Cuarto.
     *
     * @return la primera coincidencia con coordenadas, o vacío si Georef no encontró nada
     * @throws org.springframework.web.client.RestClientException si Georef no responde, tarda
     *         más que el timeout o devuelve un error (quien llama decide qué hacer)
     */
    public Optional<DireccionGeoref> buscar(String direccion) {
        JsonNode respuesta = restClient.get()
                .uri(uri -> uri.path("/direcciones")
                        .queryParam("direccion", direccion)
                        .queryParam("localidad_censal", RioCuarto.LOCALIDAD_CENSAL)
                        .queryParam("max", 1)
                        .build())
                .retrieve()
                .body(JsonNode.class);
        return parsear(respuesta);
    }

    /**
     * Lee la respuesta de Georef:
     * { "direcciones": [ { "calle": { "nombre": "SOBREMONTE" }, "nomenclatura": "...",
     *                      "ubicacion": { "lat": -33.12, "lon": -64.35 } } ], ... }
     * Las direcciones sin coordenadas (lat/lon null) se ignoran.
     */
    static Optional<DireccionGeoref> parsear(JsonNode respuesta) {
        if (respuesta == null) {
            return Optional.empty();
        }
        JsonNode direcciones = respuesta.path("direcciones");
        if (!direcciones.isArray() || direcciones.isEmpty()) {
            return Optional.empty();
        }

        JsonNode primera = direcciones.get(0);
        JsonNode latitud = primera.path("ubicacion").path("lat");
        JsonNode longitud = primera.path("ubicacion").path("lon");
        String calle = primera.path("calle").path("nombre").asText("");
        if (!latitud.isNumber() || !longitud.isNumber() || calle.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new DireccionGeoref(
                calle,
                latitud.asDouble(),
                longitud.asDouble(),
                primera.path("nomenclatura").asText("")));
    }
}
