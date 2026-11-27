package app.service;

import app.controller.dto.PropiedadRequest;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.FavoritoPropiedadRepository;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import app.storage.AlmacenamientoImagenes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Issue #154: la fecha de "disponible desde" al editar una propiedad. */
class GestionPropiedadServiceDisponibilidadTest {

    private PropiedadRepository propiedadRepository;
    private GestionPropiedadService service;
    private Propiedad propiedad;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        service = new GestionPropiedadService(propiedadRepository, usuarioRepository,
                mock(FavoritoPropiedadRepository.class), mock(SolicitudVisitaRepository.class),
                mock(AlmacenamientoImagenes.class));

        Usuario publicador = new Usuario();
        publicador.setId(1L);
        publicador.setEmail("pub@test.com");
        when(usuarioRepository.findByEmail("pub@test.com")).thenReturn(Optional.of(publicador));

        propiedad = new Propiedad();
        propiedad.setId(10L);
        propiedad.setUsuario(publicador);
        propiedad.setImagenes(new ArrayList<>());
        when(propiedadRepository.findById(10L)).thenReturn(Optional.of(propiedad));
        when(propiedadRepository.save(any(Propiedad.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static PropiedadRequest request(LocalDate disponibleDesde) {
        PropiedadRequest r = new PropiedadRequest();
        r.setTitulo("Depto");
        r.setPrecio(new BigDecimal("350000"));
        r.setMoneda(Moneda.ARS);
        r.setCalle("Sobremonte");
        r.setAltura("845");
        r.setDisponibleDesde(disponibleDesde);
        return r;
    }

    @Test
    void sePuedeCambiarLaFechaAUnaFutura() {
        LocalDate futura = Propiedad.hoy().plusDays(20);

        service.editar(10L, request(futura), "pub@test.com");

        assertEquals(futura, propiedad.getDisponibleDesde());
    }

    @Test
    void sePuedeQuitarLaFechaParaQueQuedeDisponibleYa() {
        propiedad.setDisponibleDesde(Propiedad.hoy().plusDays(20));

        service.editar(10L, request(null), "pub@test.com");

        assertNull(propiedad.getDisponibleDesde());
    }

    @Test
    void sePuedeEditarLoDemasConLaMismaFechaYaVencida() {
        // La publicó con fecha futura y ya pasó: el formulario manda la misma fecha y no debe fallar
        LocalDate vencida = Propiedad.hoy().minusDays(5);
        propiedad.setDisponibleDesde(vencida);

        service.editar(10L, request(vencida), "pub@test.com");

        assertEquals(vencida, propiedad.getDisponibleDesde());
        verify(propiedadRepository).save(propiedad);
    }

    @Test
    void unaFechaVencidaDistintaDeLaQueTeniaDa400YNoGuarda() {
        propiedad.setDisponibleDesde(Propiedad.hoy().minusDays(5));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.editar(10L, request(Propiedad.hoy().minusDays(2)), "pub@test.com"));

        assertEquals(400, ex.getStatusCode().value());
        verify(propiedadRepository, never()).save(any(Propiedad.class));
    }
}
