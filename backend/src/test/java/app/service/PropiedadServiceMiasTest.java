package app.service;

import app.controller.dto.PropiedadDetalleResponse;
import app.model.Inmobiliaria;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Issue #55: GET /propiedades/mias devuelve solo las propiedades del publicador autenticado. */
class PropiedadServiceMiasTest {

    private PropiedadRepository propiedadRepository;
    private UsuarioRepository usuarioRepository;
    private PropiedadService service;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new PropiedadService(propiedadRepository, usuarioRepository, mock(AlertaService.class));
    }

    private Usuario usuario(Long id, Inmobiliaria inmobiliaria) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail("pub@test.com");
        usuario.setInmobiliaria(inmobiliaria);
        return usuario;
    }

    private Propiedad propiedad(Long id) {
        Propiedad p = new Propiedad();
        p.setId(id);
        p.setTitulo("Depto " + id);
        p.setMoneda(Moneda.ARS);
        return p;
    }

    @Test
    void sinInmobiliariaDevuelveSoloLasDelUsuario() {
        when(usuarioRepository.findByEmail("pub@test.com")).thenReturn(Optional.of(usuario(1L, null)));
        when(propiedadRepository.findByUsuarioIdOrderByIdDesc(1L)).thenReturn(List.of(propiedad(10L)));

        List<PropiedadDetalleResponse> mias = service.listarMias("pub@test.com");

        assertEquals(1, mias.size());
        assertEquals(10L, mias.get(0).getId());
        assertNull(mias.get(0).getContacto());
        verify(propiedadRepository, never()).findDelUsuarioOSuInmobiliaria(anyLong(), anyLong());
        verify(propiedadRepository, never()).findAll();
    }

    @Test
    void conInmobiliariaIncluyeLasDeLaInmobiliaria() {
        Inmobiliaria inmobiliaria = new Inmobiliaria();
        inmobiliaria.setId(7L);
        when(usuarioRepository.findByEmail("pub@test.com")).thenReturn(Optional.of(usuario(1L, inmobiliaria)));
        when(propiedadRepository.findDelUsuarioOSuInmobiliaria(1L, 7L))
                .thenReturn(List.of(propiedad(11L), propiedad(10L)));

        List<PropiedadDetalleResponse> mias = service.listarMias("pub@test.com");

        assertEquals(List.of(11L, 10L), mias.stream().map(PropiedadDetalleResponse::getId).toList());
        verify(propiedadRepository, never()).findByUsuarioIdOrderByIdDesc(any());
    }

    @Test
    void usuarioInexistenteDa401() {
        when(usuarioRepository.findByEmail("pub@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listarMias("pub@test.com"));

        assertEquals(401, ex.getStatusCode().value());
    }
}
