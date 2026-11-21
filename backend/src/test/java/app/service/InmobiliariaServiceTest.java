package app.service;

import app.controller.dto.InmobiliariaRequest;
import app.controller.dto.PropiedadRequest;
import app.model.Inmobiliaria;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.InmobiliariaRepository;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Issue #61: crear una inmobiliaria, vincularla al usuario y publicar a su nombre. */
class InmobiliariaServiceTest {

    private InmobiliariaRepository inmobiliariaRepository;
    private UsuarioRepository usuarioRepository;
    private InmobiliariaService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        inmobiliariaRepository = mock(InmobiliariaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new InmobiliariaService(inmobiliariaRepository, usuarioRepository);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("pub@test.com");
        when(usuarioRepository.findByEmail("pub@test.com")).thenReturn(Optional.of(usuario));
        when(inmobiliariaRepository.save(any())).thenAnswer(inv -> {
            Inmobiliaria i = inv.getArgument(0);
            i.setId(7L);
            return i;
        });
    }

    private static InmobiliariaRequest request(String email) {
        InmobiliariaRequest r = new InmobiliariaRequest();
        r.setNombre("  Inmo Centro ");
        r.setEmail(email);
        r.setTelefono("  ");
        return r;
    }

    private static HttpStatus statusDe(ResponseStatusException e) {
        return HttpStatus.valueOf(e.getStatusCode().value());
    }

    @Test
    void creaLaInmobiliariaYDejaAlUsuarioAsociado() {
        Inmobiliaria creada = service.crearYVincular(request(" Contacto@InmoCentro.com "), "pub@test.com");

        assertEquals(7L, creada.getId());
        assertEquals("Inmo Centro", creada.getNombre());
        assertEquals("contacto@inmocentro.com", creada.getEmail());
        assertNull(creada.getTelefono());
        assertFalse(creada.isVerificada());
        assertSame(creada, usuario.getInmobiliaria());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void usuarioYaAsociadoRecibe409() {
        usuario.setInmobiliaria(new Inmobiliaria());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.crearYVincular(request("a@b.com"), "pub@test.com"));

        assertEquals(HttpStatus.CONFLICT, statusDe(e));
        verify(inmobiliariaRepository, never()).save(any());
    }

    @Test
    void emailDeInmobiliariaRepetidoRecibe409() {
        when(inmobiliariaRepository.existsByEmail("a@b.com")).thenReturn(true);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.crearYVincular(request("A@b.com"), "pub@test.com"));

        assertEquals(HttpStatus.CONFLICT, statusDe(e));
        verify(inmobiliariaRepository, never()).save(any());
        assertNull(usuario.getInmobiliaria());
    }

    @Test
    void lasPropiedadesQuePubliqueQuedanAsociadasALaInmobiliaria() {
        PropiedadRepository propiedadRepository = mock(PropiedadRepository.class);
        when(propiedadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PropiedadService propiedadService =
                new PropiedadService(propiedadRepository, usuarioRepository, mock(AlertaService.class));

        Inmobiliaria inmobiliaria = service.crearYVincular(request("a@b.com"), "pub@test.com");
        Propiedad publicada = propiedadService.publicar(propiedadRequest(), "pub@test.com");

        assertSame(inmobiliaria, publicada.getInmobiliaria());
        assertSame(usuario, publicada.getUsuario());
    }

    @Test
    void sinInmobiliariaLaPropiedadQuedaSoloDelUsuario() {
        PropiedadRepository propiedadRepository = mock(PropiedadRepository.class);
        when(propiedadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PropiedadService propiedadService =
                new PropiedadService(propiedadRepository, usuarioRepository, mock(AlertaService.class));

        Propiedad publicada = propiedadService.publicar(propiedadRequest(), "pub@test.com");

        assertNull(publicada.getInmobiliaria());
        assertSame(usuario, publicada.getUsuario());
    }

    private static PropiedadRequest propiedadRequest() {
        PropiedadRequest r = new PropiedadRequest();
        r.setTitulo("Depto");
        r.setPrecio(BigDecimal.TEN);
        r.setMoneda(Moneda.ARS);
        r.setCalle("San Martín");
        r.setAltura("100");
        return r;
    }
}
