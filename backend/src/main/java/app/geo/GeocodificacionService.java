package app.geo;

import app.controller.dto.GeocodificacionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Sugerencia de ubicación para el pin del mapa al publicar (issue #140).
 *
 * Es solo una sugerencia: la ubicación que se guarda es la que confirma quien publica. Por eso,
 * ante cualquier duda (Georef no encuentra la calle, devuelve otra parecida, falla o tarda) se
 * responde sin sugerencia y el mapa arranca centrado en Río Cuarto.
 */
@Service
public class GeocodificacionService {

    private static final Logger log = LoggerFactory.getLogger(GeocodificacionService.class);

    // "Av. ", "Avenida ", "Bv. ", "Calle ", "Pje. " al comienzo (con o sin punto, en cualquier mayúscula)
    private static final Pattern PREFIJO = Pattern.compile(
            "^(avenida|av|bulevar|boulevard|bv|calle|pasaje|pje)\\.?\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIACRITICOS = Pattern.compile("\\p{M}+");
    private static final Pattern NO_ALFANUMERICO = Pattern.compile("[^A-Z0-9]+");

    private final GeorefClient georefClient;

    public GeocodificacionService(GeorefClient georefClient) {
        this.georefClient = georefClient;
    }

    /**
     * @param calle  tal como la escribió el usuario ("Av. Marcelo T. de Alvear")
     * @param altura opcional ("1200")
     * @return la ubicación sugerida, o vacío si no hay una confiable
     */
    public Optional<GeocodificacionResponse> sugerir(String calle, String altura) {
        if (calle == null || calle.isBlank()) {
            return Optional.empty();
        }
        String original = calle.trim();
        String sufijoAltura = altura == null || altura.isBlank() ? "" : " " + altura.trim();

        try {
            Optional<GeocodificacionResponse> sugerencia = intentar(original, sufijoAltura);
            if (sugerencia.isPresent()) {
                return sugerencia;
            }

            // Segundo intento sin prefijos ni puntos: "Av. Marcelo T. de Alvear" → "Marcelo T de Alvear".
            // Nunca se busca solo por el apellido: "ALVEAR" y "MARCELO T DE ALVEAR" son calles distintas.
            String limpia = limpiar(original);
            if (!limpia.isBlank() && !limpia.equalsIgnoreCase(original)) {
                return intentar(limpia, sufijoAltura);
            }
            return Optional.empty();
        } catch (RuntimeException e) {
            // Georef caído, lento o con una respuesta inesperada: se sigue sin sugerencia
            log.warn("No se pudo consultar Georef para \"{}{}\": {}", original, sufijoAltura, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<GeocodificacionResponse> intentar(String calle, String sufijoAltura) {
        return georefClient.buscar(calle + sufijoAltura)
                .filter(encontrada -> coincide(calle, encontrada.calle()))
                .filter(encontrada -> RioCuarto.contiene(encontrada.latitud(), encontrada.longitud()))
                .map(encontrada -> new GeocodificacionResponse(
                        encontrada.latitud(),
                        encontrada.longitud(),
                        encontrada.nomenclatura().isBlank()
                                ? encontrada.calle() + sufijoAltura
                                : encontrada.nomenclatura()));
    }

    /** Saca el prefijo (Av., Avenida, Bv., Calle, Pje.) y los puntos, conservando las iniciales. */
    static String limpiar(String calle) {
        String sinPrefijo = PREFIJO.matcher(calle.trim()).replaceFirst("");
        return sinPrefijo.replace(".", " ").trim().replaceAll("\\s+", " ");
    }

    /**
     * Georef a veces devuelve una calle parecida pero distinta. Se acepta solo si cada palabra que
     * escribió el usuario es el comienzo de alguna palabra del nombre oficial (sin tildes y en
     * mayúsculas): "Sobremonte" → SOBREMONTE sí; "Marcelo T de Alvear" → ALVEAR no.
     */
    static boolean coincide(String escrita, String oficial) {
        List<String> palabrasEscritas = palabras(escrita);
        List<String> palabrasOficiales = palabras(oficial);
        if (palabrasEscritas.isEmpty()) {
            return false;
        }
        return palabrasEscritas.stream()
                .allMatch(palabra -> palabrasOficiales.stream().anyMatch(oficialP -> oficialP.startsWith(palabra)));
    }

    private static List<String> palabras(String texto) {
        String sinTildes = DIACRITICOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        String normalizado = NO_ALFANUMERICO.matcher(sinTildes.toUpperCase(Locale.ROOT)).replaceAll(" ").trim();
        return normalizado.isEmpty() ? List.of() : Arrays.asList(normalizado.split(" "));
    }
}
