package app.geo;

import app.controller.dto.GeocodificacionResponse;
import app.geo.GeorefClient.DireccionGeoref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Sugerencia de ubicación con Georef simulado (issue #140). */
class GeocodificacionServiceTest {

    private GeorefClient georef;
    private GeocodificacionService service;

    @BeforeEach
    void setUp() {
        georef = mock(GeorefClient.class);
        service = new GeocodificacionService(georef);
        when(georef.buscar(anyString())).thenReturn(Optional.empty());
    }

    private static Optional<DireccionGeoref> encontrada(String calle, String nomenclatura) {
        return Optional.of(new DireccionGeoref(calle, -33.1251, -64.3489, nomenclatura));
    }

    @Test
    void sobremonte845DevuelveLaUbicacion() {
        when(georef.buscar("Sobremonte 845"))
                .thenReturn(encontrada("SOBREMONTE", "SOBREMONTE 845, Río Cuarto, Río Cuarto, Córdoba"));

        Optional<GeocodificacionResponse> sugerencia = service.sugerir("Sobremonte", "845");

        assertTrue(sugerencia.isPresent());
        assertEquals(-33.1251, sugerencia.get().latitud());
        assertEquals(-64.3489, sugerencia.get().longitud());
        assertEquals("SOBREMONTE 845, Río Cuarto, Río Cuarto, Córdoba", sugerencia.get().direccion());
        verify(georef, times(1)).buscar(anyString());
    }

    @Test
    void conPrefijoYPuntosSeReintentaLimpiaConservandoLasIniciales() {
        when(georef.buscar("Marcelo T de Alvear 1200"))
                .thenReturn(encontrada("MARCELO T DE ALVEAR", "MARCELO T DE ALVEAR 1200, Río Cuarto"));

        Optional<GeocodificacionResponse> sugerencia = service.sugerir("Av. Marcelo T. de Alvear", "1200");

        assertTrue(sugerencia.isPresent());
        verify(georef).buscar("Av. Marcelo T. de Alvear 1200");
        verify(georef).buscar("Marcelo T de Alvear 1200");
    }

    @Test
    void siGeorefDevuelveOtraCalleParecidaSeDescarta() {
        // En Río Cuarto ALVEAR y MARCELO T DE ALVEAR son calles distintas
        when(georef.buscar("Marcelo T de Alvear 1200")).thenReturn(encontrada("ALVEAR", "ALVEAR 1200, Río Cuarto"));

        Optional<GeocodificacionResponse> sugerencia = service.sugerir("Marcelo T de Alvear", "1200");

        assertFalse(sugerencia.isPresent());
        // No hay nada que limpiar, así que no se reintenta (y nunca se busca solo "Alvear")
        verify(georef, times(1)).buscar(anyString());
        verify(georef, never()).buscar("Alvear 1200");
    }

    @Test
    void siGeorefEstaCaidoSeRespondeSinSugerenciaYNoSeReintenta() {
        when(georef.buscar(anyString())).thenThrow(new ResourceAccessException("Read timed out"));

        Optional<GeocodificacionResponse> sugerencia = service.sugerir("Av. Sobremonte", "845");

        assertFalse(sugerencia.isPresent());
        verify(georef, times(1)).buscar(anyString());
    }

    @Test
    void unResultadoFueraDeRioCuartoSeDescarta() {
        when(georef.buscar("Sobremonte 845"))
                .thenReturn(Optional.of(new DireccionGeoref("SOBREMONTE", -31.41, -64.18, "SOBREMONTE 845, Córdoba")));

        assertFalse(service.sugerir("Sobremonte", "845").isPresent());
    }

    @Test
    void sinCalleNoConsultaGeoref() {
        assertFalse(service.sugerir("  ", "845").isPresent());
        verify(georef, never()).buscar(anyString());
    }

    @Test
    void sinAlturaBuscaSoloLaCalle() {
        when(georef.buscar("Sobremonte")).thenReturn(encontrada("SOBREMONTE", ""));

        Optional<GeocodificacionResponse> sugerencia = service.sugerir("Sobremonte", null);

        assertTrue(sugerencia.isPresent());
        assertEquals("SOBREMONTE", sugerencia.get().direccion());
    }

    @Test
    void limpiarSacaPrefijosYPuntos() {
        assertEquals("Marcelo T de Alvear", GeocodificacionService.limpiar("Av. Marcelo T. de Alvear"));
        assertEquals("Sobremonte", GeocodificacionService.limpiar("Avenida Sobremonte"));
        assertEquals("Roca", GeocodificacionService.limpiar("Bv. Roca"));
        assertEquals("Constitución", GeocodificacionService.limpiar("calle Constitución"));
        assertEquals("Los Pinos", GeocodificacionService.limpiar("Pje. Los Pinos"));
        assertEquals("Avellaneda", GeocodificacionService.limpiar("Avellaneda"));
    }

    @Test
    void coincideSinTildesYPorComienzoDePalabra() {
        assertTrue(GeocodificacionService.coincide("Constitucion", "CONSTITUCIÓN"));
        assertTrue(GeocodificacionService.coincide("marcelo t de alvear", "MARCELO T DE ALVEAR"));
        assertTrue(GeocodificacionService.coincide("Sobrem", "SOBREMONTE"));
        assertFalse(GeocodificacionService.coincide("Marcelo T de Alvear", "ALVEAR"));
        assertFalse(GeocodificacionService.coincide("Belgrano", "SOBREMONTE"));
        assertFalse(GeocodificacionService.coincide("...", "SOBREMONTE"));
    }
}
