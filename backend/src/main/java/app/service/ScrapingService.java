package app.service;

import app.model.FuenteExterna;
import app.model.Moneda;
import app.model.Propiedad;
import app.scraping.ExtractorPropiedades;
import app.scraping.PropiedadExterna;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Obtiene propiedades desde una fuente externa y las transforma al modelo
 * interno. No las persiste.
 */
@Service
public class ScrapingService {

    private static final Pattern DIRECCION = Pattern.compile("^(.*?)\\s+(\\d+\\S*)$");

    private final List<ExtractorPropiedades> extractores;

    public ScrapingService(List<ExtractorPropiedades> extractores) {
        this.extractores = extractores;
    }

    public List<Propiedad> obtenerPropiedades(FuenteExterna fuente) {
        if (!fuente.isActiva()) {
            throw new IllegalStateException("La fuente " + fuente.getNombre() + " está inactiva");
        }

        ExtractorPropiedades extractor = extractores.stream()
                .filter(e -> e.soporta(fuente))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay extractor para la fuente " + fuente.getNombre()));

        return extractor.extraer(fuente).stream()
                .map(this::mapear)
                .toList();
    }

    Propiedad mapear(PropiedadExterna externa) {
        Propiedad propiedad = new Propiedad();
        propiedad.setTitulo(externa.titulo());
        propiedad.setDescripcion(externa.descripcion());
        propiedad.setHabitaciones(externa.habitaciones());
        propiedad.setMetrosCuadrados(externa.metrosCuadrados());
        propiedad.setUrlOriginal(externa.url());
        mapearPrecio(externa.precio(), propiedad);
        mapearDireccion(externa.direccion(), propiedad);
        return propiedad;
    }

    // "USD 1.250" / "U$S 600" -> USD; "$ 450.000" -> ARS
    private void mapearPrecio(String texto, Propiedad propiedad) {
        if (texto == null || texto.isBlank()) return;

        String upper = texto.toUpperCase();
        propiedad.setMoneda(upper.contains("USD") || upper.contains("U$S") ? Moneda.USD : Moneda.ARS);

        String digitos = texto.replaceAll("[^0-9,]", "").replace(',', '.');
        if (!digitos.isEmpty()) {
            propiedad.setPrecio(new BigDecimal(digitos));
        }
    }

    // "Av. Santa Fe 3200" -> calle "Av. Santa Fe", altura "3200"
    private void mapearDireccion(String direccion, Propiedad propiedad) {
        if (direccion == null || direccion.isBlank()) return;

        Matcher m = DIRECCION.matcher(direccion.trim());
        if (m.matches()) {
            propiedad.setCalle(m.group(1));
            propiedad.setAltura(m.group(2));
        } else {
            propiedad.setCalle(direccion.trim());
        }
    }
}
