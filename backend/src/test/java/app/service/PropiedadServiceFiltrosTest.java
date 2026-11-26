package app.service;

import app.model.EstadoPropiedad;
import app.model.Moneda;
import app.model.Propiedad;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Issue #58: filtros opcionales en el listado público de propiedades (solo las disponibles). */
class PropiedadServiceFiltrosTest {

    private PropiedadRepository propiedadRepository;
    private PropiedadService service;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        service = new PropiedadService(propiedadRepository, mock(UsuarioRepository.class), mock(AlertaService.class));
    }

    @Test
    void sinParametrosDevuelveTodasLasDisponibles() {
        // Siempre se consulta con la Specification: aunque no haya filtros, deja afuera las alquiladas
        when(propiedadRepository.findAll(ArgumentMatchers.<Specification<Propiedad>>any()))
                .thenReturn(List.of(new Propiedad(), new Propiedad()));

        assertEquals(2, service.listar(null, null, null, null, null).size());
        verify(propiedadRepository, never()).findAll();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void elListadoDejaAfueraLasAlquiladas() {
        ArgumentCaptor<Specification<Propiedad>> captor = ArgumentCaptor.forClass((Class) Specification.class);
        when(propiedadRepository.findAll(captor.capture())).thenReturn(List.of());

        service.listar(null, null, null, null, null);

        Root<Propiedad> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> estado = mock(Path.class);
        when(root.get("estado")).thenReturn(estado);

        captor.getValue().toPredicate(root, mock(CriteriaQuery.class), cb);

        // estado = DISPONIBLE, o null en las publicadas antes de que existiera el campo
        verify(cb).isNull(estado);
        verify(cb).equal(estado, EstadoPropiedad.DISPONIBLE);
    }

    @Test
    void calleEnBlancoSeIgnora() {
        when(propiedadRepository.findAll(ArgumentMatchers.<Specification<Propiedad>>any()))
                .thenReturn(List.of(new Propiedad()));

        assertEquals(1, service.listar(null, null, null, null, "   ").size());
    }

    @Test
    void conFiltrosCombinadosUsaLaSpecification() {
        when(propiedadRepository.findAll(ArgumentMatchers.<Specification<Propiedad>>any()))
                .thenReturn(List.of(new Propiedad()));

        List<?> resultado = service.listar(new BigDecimal("100000"), new BigDecimal("300000"),
                Moneda.ARS, 2, "Sarmiento");

        assertEquals(1, resultado.size());
        verify(propiedadRepository, never()).findAll();
    }

    @Test
    void precioMinimoMayorAlMaximoDa400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listar(new BigDecimal("500"), new BigDecimal("100"), null, null, null));

        assertEquals(400, ex.getStatusCode().value());
    }
}
