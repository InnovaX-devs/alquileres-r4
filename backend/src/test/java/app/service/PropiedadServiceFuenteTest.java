package app.service;

import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.Fuente;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Issue #43: diferenciar propiedades creadas por usuarios (MANUAL) de importadas (WEB). */
class PropiedadServiceFuenteTest {

    private PropiedadRepository propiedadRepository;
    private UsuarioRepository usuarioRepository;
    private PropiedadService service;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new PropiedadService(propiedadRepository, usuarioRepository, mock(AlertaService.class));
    }

    private Propiedad propiedad(Long id, Fuente fuente) {
        Propiedad p = new Propiedad();
        p.setId(id);
        p.setTitulo("Depto " + id);
        p.setMoneda(Moneda.ARS);
        p.setFuente(fuente);
        return p;
    }

    @Test
    void publicarMarcaLaPropiedadComoManual() {
        Usuario usuario = new Usuario();
        usuario.setEmail("publicador@test.com");
        when(usuarioRepository.findByEmail("publicador@test.com")).thenReturn(Optional.of(usuario));
        when(propiedadRepository.save(any(Propiedad.class))).thenAnswer(inv -> inv.getArgument(0));

        PropiedadRequest request = new PropiedadRequest();
        request.setTitulo("Depto centro");
        request.setMoneda(Moneda.ARS);

        assertEquals(Fuente.MANUAL, service.publicar(request, "publicador@test.com").getFuente());
    }

    @Test
    void detalleExponeFuenteYUrlOriginalDeImportadas() {
        Propiedad importada = propiedad(1L, Fuente.WEB);
        importada.setUrlOriginal("https://portal.com/aviso/1");
        when(propiedadRepository.findById(1L)).thenReturn(Optional.of(importada));

        PropiedadDetalleResponse detalle = service.getDetalle(1L, null);

        assertEquals("WEB", detalle.getFuente());
        assertEquals("https://portal.com/aviso/1", detalle.getUrlOriginal());
    }

    @Test
    void listadoPermiteDiferenciarPropiasDeExternas() {
        when(propiedadRepository.findAll()).thenReturn(List.of(
                propiedad(1L, Fuente.MANUAL), propiedad(2L, Fuente.WEB)));

        List<String> fuentes = service.listar().stream().map(PropiedadDetalleResponse::getFuente).toList();

        assertEquals(List.of("MANUAL", "WEB"), fuentes);
    }

    @Test
    void propiedadesSinFuenteSeConsideranManuales() {
        when(propiedadRepository.findById(3L)).thenReturn(Optional.of(propiedad(3L, null)));

        PropiedadDetalleResponse detalle = service.getDetalle(3L, null);

        assertEquals("MANUAL", detalle.getFuente());
        assertNull(detalle.getUrlOriginal());
    }
}
