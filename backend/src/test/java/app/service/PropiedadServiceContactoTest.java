package app.service;

import app.controller.dto.ContactoPublicadorResponse;
import app.controller.dto.PropiedadDetalleResponse;
import app.model.Inmobiliaria;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Issue #45: solo usuarios verificados acceden a los datos de contacto del publicador. */
class PropiedadServiceContactoTest {

    private PropiedadRepository propiedadRepository;
    private UsuarioRepository usuarioRepository;
    private PropiedadService service;

    private Usuario publicador;
    private Propiedad propiedad;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new PropiedadService(propiedadRepository, usuarioRepository, mock(AlertaService.class));

        publicador = usuario(1L, "publicador@test.com", "Carlos Publicador", "3584111111", true);

        propiedad = new Propiedad();
        propiedad.setId(10L);
        propiedad.setTitulo("Depto centro");
        propiedad.setMoneda(Moneda.ARS);
        propiedad.setUsuario(publicador);

        when(propiedadRepository.findById(10L)).thenReturn(Optional.of(propiedad));
    }

    private Usuario usuario(Long id, String email, String nombre, String telefono, boolean verificado) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setNombre(nombre);
        u.setTelefono(telefono);
        u.setVerificado(verificado);
        return u;
    }

    private Inmobiliaria inmobiliaria() {
        Inmobiliaria i = new Inmobiliaria();
        i.setId(5L);
        i.setNombre("Inmobiliaria Centro");
        i.setEmail("contacto@centro.com");
        i.setTelefono("3584222222");
        return i;
    }

    @Test
    void sinAutenticarNoVeContacto() {
        PropiedadDetalleResponse detalle = service.getDetalle(10L, null);

        assertNull(detalle.getContacto());
        assertEquals("Depto centro", detalle.getTitulo(), "el resto del detalle sigue siendo público");
    }

    @Test
    void usuarioNoVerificadoNoVeContacto() {
        Usuario visitante = usuario(2L, "visitante@test.com", "Vale", null, false);
        when(usuarioRepository.findByEmail("visitante@test.com")).thenReturn(Optional.of(visitante));

        assertNull(service.getDetalle(10L, "visitante@test.com").getContacto());
    }

    @Test
    void tokenDeUsuarioInexistenteNoVeContacto() {
        when(usuarioRepository.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

        assertNull(service.getDetalle(10L, "fantasma@test.com").getContacto());
    }

    @Test
    void usuarioVerificadoVeContactoDelPublicador() {
        Usuario visitante = usuario(2L, "visitante@test.com", "Vale", "3584333333", true);
        when(usuarioRepository.findByEmail("visitante@test.com")).thenReturn(Optional.of(visitante));

        ContactoPublicadorResponse contacto = service.getDetalle(10L, "visitante@test.com").getContacto();

        assertNotNull(contacto);
        assertEquals("USUARIO", contacto.getTipo());
        assertEquals("publicador@test.com", contacto.getEmail());
        assertEquals("3584111111", contacto.getTelefono());
    }

    @Test
    void usuarioVerificadoVeContactoDeLaInmobiliaria() {
        propiedad.setUsuario(null);
        propiedad.setInmobiliaria(inmobiliaria());
        Usuario visitante = usuario(2L, "visitante@test.com", "Vale", "3584333333", true);
        when(usuarioRepository.findByEmail("visitante@test.com")).thenReturn(Optional.of(visitante));

        ContactoPublicadorResponse contacto = service.getDetalle(10L, "visitante@test.com").getContacto();

        assertNotNull(contacto);
        assertEquals("INMOBILIARIA", contacto.getTipo());
        assertEquals("contacto@centro.com", contacto.getEmail());
        assertEquals(5L, contacto.getInmobiliariaId(), "el front lo usa para el botón de favorito (#83)");
    }

    @Test
    void siHayInmobiliariaYUsuarioSeMuestraLaInmobiliaria() {
        propiedad.setInmobiliaria(inmobiliaria());
        Usuario visitante = usuario(2L, "visitante@test.com", "Vale", "3584333333", true);
        when(usuarioRepository.findByEmail("visitante@test.com")).thenReturn(Optional.of(visitante));

        assertEquals("INMOBILIARIA", service.getDetalle(10L, "visitante@test.com").getContacto().getTipo());
    }

    @Test
    void listadoPublicoNuncaIncluyeContacto() {
        when(propiedadRepository.findAll()).thenReturn(java.util.List.of(propiedad));

        service.listar().forEach(p -> assertNull(p.getContacto()));
    }
}
