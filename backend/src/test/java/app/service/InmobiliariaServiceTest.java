package app.service;

import app.controller.dto.ActualizarInmobiliariaRequest;
import app.controller.dto.InmobiliariaRequest;
import app.controller.dto.PropiedadRequest;
import app.model.Inmobiliaria;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.RolInmobiliaria;
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

/** Issue #61: inmobiliarias, sus miembros y su verificación. */
class InmobiliariaServiceTest {

    private InmobiliariaRepository inmobiliariaRepository;
    private UsuarioRepository usuarioRepository;
    private InmobiliariaService service;

    private Usuario usuario;        // pub@test.com, sin inmobiliaria
    private Inmobiliaria inmo;      // id 7
    private Usuario administrador;  // admin@inmo.com, ADMINISTRADOR de inmo
    private Usuario agente;         // agente@inmo.com, AGENTE de inmo

    @BeforeEach
    void setUp() {
        inmobiliariaRepository = mock(InmobiliariaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new InmobiliariaService(inmobiliariaRepository, usuarioRepository);

        usuario = usuario(1L, "pub@test.com", null, null);

        inmo = new Inmobiliaria();
        inmo.setId(7L);
        inmo.setNombre("Inmo Centro");
        inmo.setEmail("contacto@inmo.com");
        administrador = usuario(2L, "admin@inmo.com", inmo, RolInmobiliaria.ADMINISTRADOR);
        agente = usuario(3L, "agente@inmo.com", inmo, RolInmobiliaria.AGENTE);

        when(inmobiliariaRepository.findById(7L)).thenReturn(Optional.of(inmo));
        when(inmobiliariaRepository.save(any())).thenAnswer(inv -> {
            Inmobiliaria i = inv.getArgument(0);
            if (i.getId() == null) i.setId(8L);
            return i;
        });
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario usuario(Long id, String email, Inmobiliaria inmobiliaria, RolInmobiliaria rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setInmobiliaria(inmobiliaria);
        u.setRolInmobiliaria(rol);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(u));
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(u));
        return u;
    }

    private static InmobiliariaRequest request(String email) {
        InmobiliariaRequest r = new InmobiliariaRequest();
        r.setNombre("  Inmo Norte ");
        r.setEmail(email);
        r.setTelefono("  ");
        return r;
    }

    private static HttpStatus statusDe(ResponseStatusException e) {
        return HttpStatus.valueOf(e.getStatusCode().value());
    }

    private static HttpStatus statusDe(Runnable accion) {
        return statusDe(assertThrows(ResponseStatusException.class, accion::run));
    }

    // ── Alta ─────────────────────────────────────────────────────────────────

    @Test
    void quienCreaLaInmobiliariaQuedaComoAdministrador() {
        Inmobiliaria creada = service.crearYVincular(request(" Norte@Inmo.com "), "pub@test.com");

        assertEquals("Inmo Norte", creada.getNombre());
        assertEquals("norte@inmo.com", creada.getEmail());
        assertNull(creada.getTelefono());
        assertFalse(creada.isVerificada(), "arranca pendiente de verificación");
        assertSame(creada, usuario.getInmobiliaria());
        assertEquals(RolInmobiliaria.ADMINISTRADOR, usuario.getRolInmobiliaria());
    }

    @Test
    void noSePuedeCrearSiYaPerteneceAUna() {
        assertEquals(HttpStatus.CONFLICT, statusDe(() -> service.crearYVincular(request("x@y.com"), "agente@inmo.com")));
    }

    @Test
    void emailDeInmobiliariaRepetidoDa409() {
        when(inmobiliariaRepository.existsByEmail("x@y.com")).thenReturn(true);

        assertEquals(HttpStatus.CONFLICT, statusDe(() -> service.crearYVincular(request("X@y.com"), "pub@test.com")));
        assertNull(usuario.getInmobiliaria());
    }

    // ── Editar datos ─────────────────────────────────────────────────────────

    @Test
    void elAdministradorEditaYSiCambiaElNombreVuelveAPendiente() {
        inmo.setVerificada(true);
        ActualizarInmobiliariaRequest r = new ActualizarInmobiliariaRequest();
        r.setNombre("Inmo Centro SRL");
        r.setTelefono("358-111");

        Inmobiliaria actualizada = service.actualizar(7L, r, "admin@inmo.com");

        assertEquals("Inmo Centro SRL", actualizada.getNombre());
        assertEquals("358-111", actualizada.getTelefono());
        assertFalse(actualizada.isVerificada());
    }

    @Test
    void cambiarSoloElTelefonoMantieneLaVerificacion() {
        inmo.setVerificada(true);
        ActualizarInmobiliariaRequest r = new ActualizarInmobiliariaRequest();
        r.setTelefono("358-111");

        assertTrue(service.actualizar(7L, r, "admin@inmo.com").isVerificada());
    }

    @Test
    void unAgenteNoPuedeEditarLaInmobiliaria() {
        ActualizarInmobiliariaRequest r = new ActualizarInmobiliariaRequest();
        r.setNombre("Hackeada");

        assertEquals(HttpStatus.FORBIDDEN, statusDe(() -> service.actualizar(7L, r, "agente@inmo.com")));
        assertEquals("Inmo Centro", inmo.getNombre());
    }

    // ── Miembros ─────────────────────────────────────────────────────────────

    @Test
    void elAdministradorVinculaAUnUsuarioComoAgente() {
        Usuario agregado = service.agregarMiembro(7L, " PUB@test.com ", "admin@inmo.com");

        assertSame(usuario, agregado);
        assertSame(inmo, usuario.getInmobiliaria());
        assertEquals(RolInmobiliaria.AGENTE, usuario.getRolInmobiliaria());
    }

    @Test
    void unAgenteNoPuedeVincularMiembros() {
        assertEquals(HttpStatus.FORBIDDEN, statusDe(() -> service.agregarMiembro(7L, "pub@test.com", "agente@inmo.com")));
        assertNull(usuario.getInmobiliaria());
    }

    @Test
    void vincularUnEmailNoRegistradoDa404() {
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> service.agregarMiembro(7L, "nadie@x.com", "admin@inmo.com")));
    }

    @Test
    void vincularAAlguienDeOtraInmobiliariaDa409() {
        Inmobiliaria otra = new Inmobiliaria();
        otra.setId(9L);
        usuario.setInmobiliaria(otra);

        assertEquals(HttpStatus.CONFLICT, statusDe(() -> service.agregarMiembro(7L, "pub@test.com", "admin@inmo.com")));
        assertSame(otra, usuario.getInmobiliaria());
    }

    @Test
    void elAdministradorDesvinculaAUnAgente() {
        service.quitarMiembro(7L, 3L, "admin@inmo.com");

        assertNull(agente.getInmobiliaria());
        assertNull(agente.getRolInmobiliaria());
    }

    @Test
    void elAdministradorNoPuedeQuitarseASiMismo() {
        assertEquals(HttpStatus.CONFLICT, statusDe(() -> service.quitarMiembro(7L, 2L, "admin@inmo.com")));
    }

    @Test
    void quitarAQuienNoEsMiembroDa404() {
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> service.quitarMiembro(7L, 1L, "admin@inmo.com")));
    }

    @Test
    void soloLosMiembrosVenLaListaDeMiembros() {
        assertEquals(HttpStatus.FORBIDDEN, statusDe(() -> service.listarMiembros(7L, "pub@test.com")));
        service.listarMiembros(7L, "agente@inmo.com");
        verify(usuarioRepository).findByInmobiliariaIdOrderByNombreAsc(7L);
    }

    // ── Verificación ─────────────────────────────────────────────────────────

    @Test
    void cambiarVerificacion() {
        assertTrue(service.cambiarVerificacion(7L, true).isVerificada());
        assertFalse(service.cambiarVerificacion(7L, false).isVerificada());
    }

    @Test
    void verificarInmobiliariaInexistenteDa404() {
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> service.cambiarVerificacion(99L, true)));
    }

    // ── Publicar a nombre de la inmobiliaria ─────────────────────────────────

    @Test
    void unAgentePublicaANombreDeLaInmobiliaria() {
        Propiedad publicada = publicarComo("agente@inmo.com");

        assertSame(inmo, publicada.getInmobiliaria());
        assertSame(agente, publicada.getUsuario());
    }

    @Test
    void unParticularPublicaSinInmobiliaria() {
        assertNull(publicarComo("pub@test.com").getInmobiliaria());
    }

    private Propiedad publicarComo(String email) {
        PropiedadRepository propiedadRepository = mock(PropiedadRepository.class);
        when(propiedadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PropiedadService propiedadService =
                new PropiedadService(propiedadRepository, usuarioRepository, mock(AlertaService.class));

        PropiedadRequest r = new PropiedadRequest();
        r.setTitulo("Depto");
        r.setPrecio(BigDecimal.TEN);
        r.setMoneda(Moneda.ARS);
        r.setCalle("San Martín");
        r.setAltura("100");
        return propiedadService.publicar(r, email);
    }
}
