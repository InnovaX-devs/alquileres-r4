package app.service;

import app.model.FuenteExterna;
import app.model.Moneda;
import app.model.Propiedad;
import app.scraping.ExtractorSimulado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScrapingServiceTest {

    private final ScrapingService service = new ScrapingService(List.of(new ExtractorSimulado()));

    @Test
    void obtienePropiedadesDeFuenteSimuladaYLasMapea() {
        List<Propiedad> propiedades = service.obtenerPropiedades(
                new FuenteExterna("Simulada", "simulado://portal"));

        assertEquals(3, propiedades.size());

        Propiedad p = propiedades.get(0);
        assertEquals("Departamento 2 ambientes en Palermo", p.getTitulo());
        assertEquals(Moneda.ARS, p.getMoneda());
        assertEquals(0, new BigDecimal("450000").compareTo(p.getPrecio()));
        assertEquals("Av. Santa Fe", p.getCalle());
        assertEquals("3200", p.getAltura());
        assertEquals("simulado://portal/propiedades/1", p.getUrlOriginal());

        Propiedad usd = propiedades.get(2);
        assertEquals(Moneda.USD, usd.getMoneda());
        assertEquals(0, new BigDecimal("1250").compareTo(usd.getPrecio()));
    }

    @Test
    void fallaSiLaFuenteEstaInactiva() {
        FuenteExterna fuente = new FuenteExterna("Simulada", "simulado://portal");
        fuente.setActiva(false);
        assertThrows(IllegalStateException.class, () -> service.obtenerPropiedades(fuente));
    }

    @Test
    void fallaSiNoHayExtractorParaLaFuente() {
        FuenteExterna fuente = new FuenteExterna("Desconocida", "https://otro-portal.com");
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPropiedades(fuente));
    }
}
