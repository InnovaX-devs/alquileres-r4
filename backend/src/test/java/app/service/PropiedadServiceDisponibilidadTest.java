package app.service;

import app.controller.dto.PropiedadRequest;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Issue #154: "disponible desde" al publicar y filtro disponibleAntesDe del listado público. */
class PropiedadServiceDisponibilidadTest {

    private PropiedadRepository propiedadRepository;
    private PropiedadService service;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        service = new PropiedadService(propiedadRepository, usuarioRepository, mock(AlertaService.class));

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("pub@test.com");
        when(usuarioRepository.findByEmail("pub@test.com")).thenReturn(Optional.of(usuario));
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

    // ── Publicar ─────────────────────────────────────────────────────────────

    @Test
    void sinFechaLaPropiedadQuedaDisponibleYa() {
        Propiedad guardada = service.publicar(request(null), "pub@test.com");

        assertNull(guardada.getDisponibleDesde());
    }

    @Test
    void conFechaDeHoyOFuturaSeGuarda() {
        LocalDate hoy = Propiedad.hoy();

        assertEquals(hoy, service.publicar(request(hoy), "pub@test.com").getDisponibleDesde());
        assertEquals(hoy.plusDays(30), service.publicar(request(hoy.plusDays(30)), "pub@test.com").getDisponibleDesde());
    }

    @Test
    void conFechaAnteriorAHoyDa400YNoGuarda() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.publicar(request(Propiedad.hoy().minusDays(1)), "pub@test.com"));

        assertEquals(400, ex.getStatusCode().value());
        verify(propiedadRepository, never()).save(any(Propiedad.class));
    }

    // ── Filtro disponibleAntesDe ─────────────────────────────────────────────

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void conDisponibleAntesDeIncluyeLasSinFechaYLasDisponiblesHastaEsaFecha() {
        ArgumentCaptor<Specification<Propiedad>> captor = ArgumentCaptor.forClass((Class) Specification.class);
        when(propiedadRepository.findAll(captor.capture())).thenReturn(List.of());
        LocalDate fecha = LocalDate.of(2026, 3, 10);

        service.listar(null, null, null, null, null, fecha);

        Root<Propiedad> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<LocalDate> disponible = mock(Path.class);
        when(root.<LocalDate>get("disponibleDesde")).thenReturn(disponible);

        captor.getValue().toPredicate(root, mock(CriteriaQuery.class), cb);

        // sin fecha (disponible ya) o con fecha igual o anterior a la pedida
        verify(cb).isNull(disponible);
        verify(cb).lessThanOrEqualTo(disponible, fecha);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void sinDisponibleAntesDeNoFiltraPorFecha() {
        ArgumentCaptor<Specification<Propiedad>> captor = ArgumentCaptor.forClass((Class) Specification.class);
        when(propiedadRepository.findAll(captor.capture())).thenReturn(List.of());

        service.listar(null, null, null, null, null);

        Root<Propiedad> root = mock(Root.class);
        captor.getValue().toPredicate(root, mock(CriteriaQuery.class), mock(CriteriaBuilder.class));

        verify(root, never()).get("disponibleDesde");
    }
}
