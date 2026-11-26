package app.service;

import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.EstadoPropiedad;
import app.model.Fuente;
import app.model.ImagenPropiedad;
import app.model.Inmobiliaria;
import app.model.Moneda;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.FavoritoPropiedadRepository;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import app.storage.AlmacenamientoImagenes;
import app.storage.AlmacenamientoImagenesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Editar, marcar como alquilada y eliminar una propiedad publicada. */
class GestionPropiedadServiceTest {

    private PropiedadRepository propiedadRepository;
    private UsuarioRepository usuarioRepository;
    private FavoritoPropiedadRepository favoritoRepository;
    private SolicitudVisitaRepository visitaRepository;
    private AlmacenamientoImagenes almacenamiento;
    private GestionPropiedadService service;

    private Usuario publicador;
    private Propiedad propiedad;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        favoritoRepository = mock(FavoritoPropiedadRepository.class);
        visitaRepository = mock(SolicitudVisitaRepository.class);
        almacenamiento = mock(AlmacenamientoImagenes.class);
        service = new GestionPropiedadService(propiedadRepository, usuarioRepository, favoritoRepository,
                visitaRepository, almacenamiento);

        publicador = usuario(1L, "pub@test.com");

        propiedad = new Propiedad();
        propiedad.setId(10L);
        propiedad.setTitulo("Depto viejo");
        propiedad.setPrecio(new BigDecimal("100000"));
        propiedad.setMoneda(Moneda.ARS);
        propiedad.setFuente(Fuente.MANUAL);
        propiedad.setUsuario(publicador);
        propiedad.setImagenes(new ArrayList<>());

        when(propiedadRepository.findById(10L)).thenReturn(Optional.of(propiedad));
        when(propiedadRepository.save(any(Propiedad.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario usuario(Long id, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(u));
        return u;
    }

    private static Inmobiliaria inmobiliaria(Long id) {
        Inmobiliaria i = new Inmobiliaria();
        i.setId(id);
        return i;
    }

    private static ImagenPropiedad imagen(Long id, String identificador) {
        ImagenPropiedad i = new ImagenPropiedad();
        i.setId(id);
        i.setUrl("https://img/" + id + ".jpg");
        i.setIdentificadorExterno(identificador);
        i.setOrden(id.intValue());
        return i;
    }

    private static PropiedadRequest request() {
        PropiedadRequest r = new PropiedadRequest();
        r.setTitulo("Depto renovado");
        r.setPrecio(new BigDecimal("150000"));
        r.setMoneda(Moneda.USD);
        r.setCalle("Sobremonte");
        r.setAltura("845");
        r.setHabitaciones(2);
        r.setLatitud(-33.1241);
        r.setLongitud(-64.3484);
        return r;
    }

    private int statusDe(Runnable accion) {
        return assertThrows(ResponseStatusException.class, accion::run).getStatusCode().value();
    }

    // ── Editar ───────────────────────────────────────────────────────────────

    @Test
    void elPublicadorPuedeEditarYRecibeLaUbicacionExacta() {
        PropiedadDetalleResponse r = service.editar(10L, request(), "pub@test.com");

        assertEquals("Depto renovado", propiedad.getTitulo());
        assertEquals(new BigDecimal("150000"), propiedad.getPrecio());
        assertEquals(Moneda.USD, propiedad.getMoneda());
        assertEquals(2, propiedad.getHabitaciones());
        assertEquals(-33.1241, r.getLatitud());
        assertNull(r.getRadioMetros());
        verify(propiedadRepository).save(propiedad);
    }

    @Test
    void editarNoCambiaQuienLaPublicoNiLaFuenteNiElEstado() {
        propiedad.setEstado(EstadoPropiedad.ALQUILADA);

        service.editar(10L, request(), "pub@test.com");

        assertSame(publicador, propiedad.getUsuario());
        assertEquals(Fuente.MANUAL, propiedad.getFuente());
        assertEquals(EstadoPropiedad.ALQUILADA, propiedad.getEstado());
    }

    @Test
    void editarSinUbicacionLaQuita() {
        propiedad.setLatitud(-33.12);
        propiedad.setLongitud(-64.34);
        PropiedadRequest r = request();
        r.setLatitud(null);
        r.setLongitud(null);

        service.editar(10L, r, "pub@test.com");

        assertNull(propiedad.getLatitud());
        assertNull(propiedad.getLongitud());
    }

    @Test
    void unTerceroNoPuedeEditar() {
        usuario(2L, "otro@test.com");

        assertEquals(403, statusDe(() -> service.editar(10L, request(), "otro@test.com")));
        assertEquals("Depto viejo", propiedad.getTitulo());
        verify(propiedadRepository, never()).save(any());
    }

    @Test
    void editarUnaPropiedadInexistenteDa404() {
        when(propiedadRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(404, statusDe(() -> service.editar(99L, request(), "pub@test.com")));
    }

    @Test
    void unMiembroDeLaInmobiliariaPuedeEditarLasDeLaInmobiliaria() {
        propiedad.setUsuario(null);
        propiedad.setInmobiliaria(inmobiliaria(5L));
        Usuario agente = usuario(3L, "agente@test.com");
        agente.setInmobiliaria(inmobiliaria(5L));

        service.editar(10L, request(), "agente@test.com");

        assertEquals("Depto renovado", propiedad.getTitulo());
    }

    // ── Estado ───────────────────────────────────────────────────────────────

    @Test
    void lasPublicadasAntesDelCampoEstadoCuentanComoDisponibles() {
        propiedad.setEstado(null);

        assertEquals(EstadoPropiedad.DISPONIBLE, propiedad.getEstado());
        assertEquals("DISPONIBLE", PropiedadDetalleResponse.from(propiedad, null).getEstado());
    }

    @Test
    void elPublicadorPuedeMarcarlaComoAlquiladaYVolverAPublicarla() {
        PropiedadDetalleResponse alquilada = service.cambiarEstado(10L, EstadoPropiedad.ALQUILADA, "pub@test.com");
        assertEquals("ALQUILADA", alquilada.getEstado());
        assertEquals(EstadoPropiedad.ALQUILADA, propiedad.getEstado());

        PropiedadDetalleResponse disponible = service.cambiarEstado(10L, EstadoPropiedad.DISPONIBLE, "pub@test.com");
        assertEquals("DISPONIBLE", disponible.getEstado());
    }

    @Test
    void unTerceroNoPuedeCambiarElEstado() {
        usuario(2L, "otro@test.com");

        assertEquals(403, statusDe(() -> service.cambiarEstado(10L, EstadoPropiedad.ALQUILADA, "otro@test.com")));
        assertEquals(EstadoPropiedad.DISPONIBLE, propiedad.getEstado());
    }

    @Test
    void estadoNuloDa400() {
        assertEquals(400, statusDe(() -> service.cambiarEstado(10L, null, "pub@test.com")));
    }

    // ── Eliminar ─────────────────────────────────────────────────────────────

    @Test
    void eliminarBorraFavoritosVisitasLaPropiedadYSusFotos() {
        propiedad.getImagenes().add(imagen(1L, "alquileres/propiedades/10/a"));
        propiedad.getImagenes().add(imagen(2L, "alquileres/propiedades/10/b"));

        service.eliminar(10L, "pub@test.com");

        verify(favoritoRepository).deleteByPropiedadId(10L);
        verify(visitaRepository).deleteByPropiedadId(10L);
        verify(propiedadRepository).delete(propiedad);
        verify(almacenamiento).eliminar("alquileres/propiedades/10/a");
        verify(almacenamiento).eliminar("alquileres/propiedades/10/b");
    }

    @Test
    void siFallaBorrarUnaFotoDelAlmacenamientoLaPropiedadSeEliminaIgual() {
        propiedad.getImagenes().add(imagen(1L, "a"));
        propiedad.getImagenes().add(imagen(2L, "b"));
        doThrow(new AlmacenamientoImagenesException("caído", new RuntimeException()))
                .when(almacenamiento).eliminar("a");

        assertDoesNotThrow(() -> service.eliminar(10L, "pub@test.com"));

        verify(propiedadRepository).delete(propiedad);
        verify(almacenamiento).eliminar("b");
    }

    @Test
    void unTerceroNoPuedeEliminar() {
        usuario(2L, "otro@test.com");

        assertEquals(403, statusDe(() -> service.eliminar(10L, "otro@test.com")));

        verify(propiedadRepository, never()).delete(any());
        verify(favoritoRepository, never()).deleteByPropiedadId(anyLong());
        verify(visitaRepository, never()).deleteByPropiedadId(anyLong());
    }

    @Test
    void unMiembroDeOtraInmobiliariaNoPuedeEliminar() {
        propiedad.setUsuario(null);
        propiedad.setInmobiliaria(inmobiliaria(5L));
        Usuario ajeno = usuario(4L, "ajeno@test.com");
        ajeno.setInmobiliaria(inmobiliaria(99L));

        assertEquals(403, statusDe(() -> service.eliminar(10L, "ajeno@test.com")));
        verify(propiedadRepository, never()).delete(any());
    }

    @Test
    void eliminarUnaPropiedadSinFotosNoLlamaAlAlmacenamiento() {
        propiedad.setImagenes(null);

        service.eliminar(10L, "pub@test.com");

        verify(propiedadRepository).delete(propiedad);
        verify(almacenamiento, never()).eliminar(any());
    }

    @Test
    void elDetalleDelPublicadorTraeLasFotosConSuId() {
        propiedad.setImagenes(new ArrayList<>(List.of(imagen(7L, "x"), imagen(3L, "y"))));

        PropiedadDetalleResponse r = PropiedadDetalleResponse.paraPublicador(propiedad);

        assertEquals(2, r.getFotos().size());
        assertEquals(3L, r.getFotos().get(0).id());
        assertNull(PropiedadDetalleResponse.from(propiedad, null).getFotos());
    }
}
