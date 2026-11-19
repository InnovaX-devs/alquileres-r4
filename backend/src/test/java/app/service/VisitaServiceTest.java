package app.service;

import app.controller.dto.CambiarEstadoVisitaRequest;
import app.model.EstadoSolicitud;
import app.model.Inmobiliaria;
import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Issue #45: solo los publicadores de la propiedad pueden gestionar las solicitudes de visita. */
class VisitaServiceTest {

    private SolicitudVisitaRepository solicitudRepository;
    private UsuarioRepository usuarioRepository;
    private VisitaService service;

    private Usuario publicador;
    private Usuario solicitante;
    private Propiedad propiedad;
    private SolicitudVisita solicitud;

    @BeforeEach
    void setUp() {
        solicitudRepository = mock(SolicitudVisitaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new VisitaService(solicitudRepository, mock(PropiedadRepository.class),
                usuarioRepository, mock(EmailService.class));

        publicador = usuario(1L, "publicador@test.com");
        solicitante = usuario(2L, "solicitante@test.com");

        propiedad = new Propiedad();
        propiedad.setId(10L);
        propiedad.setUsuario(publicador);

        solicitud = new SolicitudVisita();
        solicitud.setId(100L);
        solicitud.setUsuario(solicitante);
        solicitud.setPropiedad(propiedad);

        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudVisita.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario usuario(Long id, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(u));
        return u;
    }

    private Inmobiliaria inmobiliaria(Long id) {
        Inmobiliaria i = new Inmobiliaria();
        i.setId(id);
        return i;
    }

    private CambiarEstadoVisitaRequest request(EstadoSolicitud estado) {
        CambiarEstadoVisitaRequest r = new CambiarEstadoVisitaRequest();
        r.setEstado(estado);
        return r;
    }

    private int statusDe(Runnable accion) {
        return assertThrows(ResponseStatusException.class, accion::run).getStatusCode().value();
    }

    // ── Quién puede gestionar ────────────────────────────────────────────────

    @Test
    void elDuenioDeLaPropiedadPuedeAceptar() {
        SolicitudVisita resultado = service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "publicador@test.com");

        assertEquals(EstadoSolicitud.ACEPTADA, resultado.getEstado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void elDuenioDeLaPropiedadPuedeRechazar() {
        SolicitudVisita resultado = service.cambiarEstado(100L, request(EstadoSolicitud.RECHAZADA), "publicador@test.com");

        assertEquals(EstadoSolicitud.RECHAZADA, resultado.getEstado());
    }

    @Test
    void unTercerosNoPuedeGestionar() {
        usuario(3L, "tercero@test.com");

        int status = statusDe(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "tercero@test.com"));

        assertEquals(403, status);
        verify(solicitudRepository, never()).save(any());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
    }

    @Test
    void quienPidioLaVisitaNoPuedeGestionarSuPropiaSolicitud() {
        int status = statusDe(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "solicitante@test.com"));

        assertEquals(403, status);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void miembroDeLaInmobiliariaDeLaPropiedadPuedeGestionar() {
        propiedad.setUsuario(null);
        propiedad.setInmobiliaria(inmobiliaria(5L));
        Usuario agente = usuario(4L, "agente@test.com");
        agente.setInmobiliaria(inmobiliaria(5L));

        SolicitudVisita resultado = service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "agente@test.com");

        assertEquals(EstadoSolicitud.ACEPTADA, resultado.getEstado());
    }

    @Test
    void miembroDeOtraInmobiliariaNoPuedeGestionar() {
        propiedad.setUsuario(null);
        propiedad.setInmobiliaria(inmobiliaria(5L));
        Usuario ajeno = usuario(4L, "ajeno@test.com");
        ajeno.setInmobiliaria(inmobiliaria(99L));

        int status = statusDe(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "ajeno@test.com"));

        assertEquals(403, status);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void conUsuarioEInmobiliariaTambienGestionanLosMiembrosDeLaInmobiliaria() {
        propiedad.setInmobiliaria(inmobiliaria(5L)); // la propiedad tiene usuario Y inmobiliaria
        Usuario agente = usuario(4L, "agente@test.com");
        agente.setInmobiliaria(inmobiliaria(5L));

        SolicitudVisita resultado = service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "agente@test.com");

        assertEquals(EstadoSolicitud.ACEPTADA, resultado.getEstado());
    }

    @Test
    void usuarioSinInmobiliariaNoGestionaPropiedadDeInmobiliaria() {
        propiedad.setUsuario(null);
        propiedad.setInmobiliaria(inmobiliaria(5L));

        int status = statusDe(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "solicitante@test.com"));

        assertEquals(403, status);
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    @Test
    void noSePuedeVolverAPendiente() {
        int status = statusDe(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.PENDIENTE), "publicador@test.com"));

        assertEquals(400, status);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void estadoNuloDa400() {
        int status = statusDe(() ->
                service.cambiarEstado(100L, request(null), "publicador@test.com"));

        assertEquals(400, status);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void unNoPublicadorRecibe403AunqueElEstadoSeaInvalido() {
        // El permiso se chequea antes que la validación: no se filtra información a terceros.
        usuario(3L, "tercero@test.com");

        int status = statusDe(() ->
                service.cambiarEstado(100L, request(null), "tercero@test.com"));

        assertEquals(403, status);
    }

    @Test
    void solicitudInexistenteDa404() {
        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        int status = statusDe(() ->
                service.cambiarEstado(999L, request(EstadoSolicitud.ACEPTADA), "publicador@test.com"));

        assertEquals(404, status);
    }

    @Test
    void usuarioInexistenteDa401() {
        when(usuarioRepository.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

        int status = statusDe(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "fantasma@test.com"));

        assertEquals(401, status);
    }
}
