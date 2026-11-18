package app.scraping;

import app.model.FuenteExterna;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fuente de prueba que devuelve datos fijos. Se usa para las fuentes cuya
 * URL base empieza con "simulado://".
 */
@Component
public class ExtractorSimulado implements ExtractorPropiedades {

    static final String PREFIJO = "simulado://";

    @Override
    public boolean soporta(FuenteExterna fuente) {
        return fuente.getUrlBase() != null && fuente.getUrlBase().startsWith(PREFIJO);
    }

    @Override
    public List<PropiedadExterna> extraer(FuenteExterna fuente) {
        String base = fuente.getUrlBase();
        return List.of(
                new PropiedadExterna(
                        "Departamento 2 ambientes en Palermo",
                        "Luminoso, balcón al frente, apto mascotas.",
                        "$ 450.000",
                        "Av. Santa Fe 3200",
                        1, 45,
                        base + "/propiedades/1"),
                new PropiedadExterna(
                        "Monoambiente en Recoleta",
                        "A estrenar, con amenities.",
                        "USD 600",
                        "Junín 1500",
                        null, 30,
                        base + "/propiedades/2"),
                new PropiedadExterna(
                        "Casa 3 dormitorios en Belgrano",
                        "Patio y parrilla.",
                        "U$S 1.250",
                        "Cuba 2100",
                        3, 120,
                        base + "/propiedades/3")
        );
    }
}
