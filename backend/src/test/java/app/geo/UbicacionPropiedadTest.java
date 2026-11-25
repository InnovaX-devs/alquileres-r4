package app.geo;

import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.Moneda;
import app.model.Propiedad;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Validación de la ubicación, ubicación aproximada en público y lectura de Georef (issue #140). */
class UbicacionPropiedadTest {

    private static final double LAT = -33.12517;
    private static final double LON = -64.34893;

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static PropiedadRequest request(Double latitud, Double longitud) {
        PropiedadRequest r = new PropiedadRequest();
        r.setTitulo("Depto");
        r.setPrecio(new BigDecimal("350000"));
        r.setMoneda(Moneda.ARS);
        r.setCalle("Sobremonte");
        r.setAltura("845");
        r.setHabitaciones(2);
        r.setLatitud(latitud);
        r.setLongitud(longitud);
        return r;
    }

    private static boolean ubicacionInvalida(PropiedadRequest r) {
        Set<ConstraintViolation<PropiedadRequest>> errores = validator.validate(r);
        return errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("ubicacionValida"));
    }

    // --- Validación del request ---

    @Test
    void sinUbicacionEsValido() {
        assertFalse(ubicacionInvalida(request(null, null)));
    }

    @Test
    void conAmbasCoordenadasEnRioCuartoEsValido() {
        assertFalse(ubicacionInvalida(request(LAT, LON)));
    }

    @Test
    void conUnaSolaCoordenadaEsInvalido() {
        assertTrue(ubicacionInvalida(request(LAT, null)));
        assertTrue(ubicacionInvalida(request(null, LON)));
    }

    @Test
    void fueraDeRioCuartoEsInvalido() {
        assertTrue(ubicacionInvalida(request(-31.4201, -64.1888))); // Córdoba capital
        assertTrue(ubicacionInvalida(request(-34.6037, -58.3816))); // Buenos Aires
    }

    // --- Ubicación aproximada ---

    @Test
    void laAproximadaQuedaDentroDelCirculoDe150Metros() {
        double lat = RioCuarto.aproximarLatitud(LAT);
        double lon = RioCuarto.aproximarLongitud(LON);
        assertTrue(distanciaMetros(LAT, LON, lat, lon) < RioCuarto.RADIO_APROXIMADO_METROS);
        assertNotEquals(LAT, lat);
    }

    @Test
    void propiedadesDeLaMismaCeldaDanElMismoPunto() {
        // Dos casas a ~10 m dan la misma ubicación aproximada: no se puede deducir cuál es
        double otraLat = LAT + 0.00009;
        assertEquals(celda(LAT, RioCuarto.PASO_LATITUD), celda(otraLat, RioCuarto.PASO_LATITUD));
        assertEquals(RioCuarto.aproximarLatitud(LAT), RioCuarto.aproximarLatitud(otraLat));
    }

    @Test
    void enPublicoSeDevuelveAproximadaYAlPublicadorExacta() {
        Propiedad propiedad = new Propiedad();
        propiedad.setId(1L);
        propiedad.setLatitud(LAT);
        propiedad.setLongitud(LON);

        PropiedadDetalleResponse publica = PropiedadDetalleResponse.from(propiedad, null);
        assertEquals(RioCuarto.aproximarLatitud(LAT), publica.getLatitud());
        assertEquals(RioCuarto.aproximarLongitud(LON), publica.getLongitud());
        assertEquals(150, publica.getRadioMetros());

        PropiedadDetalleResponse delPublicador = PropiedadDetalleResponse.paraPublicador(propiedad);
        assertEquals(LAT, delPublicador.getLatitud());
        assertEquals(LON, delPublicador.getLongitud());
        assertNull(delPublicador.getRadioMetros());
    }

    @Test
    void sinUbicacionNoSeMandanCoordenadas() {
        Propiedad propiedad = new Propiedad();
        propiedad.setId(1L);

        PropiedadDetalleResponse publica = PropiedadDetalleResponse.from(propiedad, null);
        assertNull(publica.getLatitud());
        assertNull(publica.getLongitud());
        assertNull(publica.getRadioMetros());
    }

    // --- Lectura de la respuesta de Georef ---

    @Test
    void parseaLaRespuestaDeGeoref() throws Exception {
        JsonNode json = new ObjectMapper().readTree("""
                {"cantidad": 1, "direcciones": [{
                  "calle": {"id": "1409801001234", "nombre": "SOBREMONTE", "categoria": "CALLE"},
                  "altura": {"valor": 845, "unidad": null},
                  "nomenclatura": "SOBREMONTE 845, Río Cuarto, Río Cuarto, Córdoba",
                  "ubicacion": {"lat": -33.12517, "lon": -64.34893}
                }]}""");

        GeorefClient.DireccionGeoref direccion = GeorefClient.parsear(json).orElseThrow();
        assertEquals("SOBREMONTE", direccion.calle());
        assertEquals(LAT, direccion.latitud());
        assertEquals(LON, direccion.longitud());
        assertEquals("SOBREMONTE 845, Río Cuarto, Río Cuarto, Córdoba", direccion.nomenclatura());
    }

    @Test
    void sinResultadosOSinCoordenadasNoDevuelveNada() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(GeorefClient.parsear(mapper.readTree("{\"cantidad\": 0, \"direcciones\": []}")).isEmpty());
        assertTrue(GeorefClient.parsear(mapper.readTree("""
                {"direcciones": [{"calle": {"nombre": "SOBREMONTE"}, "ubicacion": {"lat": null, "lon": null}}]}""")).isEmpty());
        assertTrue(GeorefClient.parsear(null).isEmpty());
    }

    private static long celda(double valor, double paso) {
        return (long) Math.floor(valor / paso);
    }

    private static double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6_371_000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
