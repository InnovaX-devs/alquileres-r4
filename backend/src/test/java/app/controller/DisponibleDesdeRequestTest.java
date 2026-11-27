package app.controller;

import app.controller.dto.PropiedadRequest;
import app.model.Propiedad;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Issue #154: regla de la fecha de "disponible desde" en PropiedadRequest. */
class DisponibleDesdeRequestTest {

    private static final LocalDate HOY = LocalDate.of(2026, 3, 10);

    private static PropiedadRequest conFecha(LocalDate fecha) {
        PropiedadRequest r = new PropiedadRequest();
        r.setDisponibleDesde(fecha);
        return r;
    }

    @Test
    void sinFechaEsValido() {
        assertDoesNotThrow(() -> conFecha(null).validarDisponibleDesde(null, HOY));
    }

    @Test
    void hoyYUnaFechaFuturaSonValidas() {
        assertDoesNotThrow(() -> conFecha(HOY).validarDisponibleDesde(null, HOY));
        assertDoesNotThrow(() -> conFecha(HOY.plusMonths(3)).validarDisponibleDesde(null, HOY));
    }

    @Test
    void unaFechaAnteriorAHoyDa400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> conFecha(HOY.minusDays(1)).validarDisponibleDesde(null, HOY));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void alEditarSeAceptaLaMismaFechaQueYaTeniaAunqueHayaPasado() {
        LocalDate vencida = HOY.minusDays(5);

        assertDoesNotThrow(() -> conFecha(vencida).validarDisponibleDesde(vencida, HOY));
    }

    @Test
    void alEditarNoSeAceptaOtraFechaVencida() {
        assertThrows(ResponseStatusException.class,
                () -> conFecha(HOY.minusDays(2)).validarDisponibleDesde(HOY.minusDays(5), HOY));
    }

    @Test
    void aplicarACopiaLaFecha() {
        Propiedad propiedad = new Propiedad();

        conFecha(HOY.plusDays(7)).aplicarA(propiedad);

        assertEquals(HOY.plusDays(7), propiedad.getDisponibleDesde());
    }
}
