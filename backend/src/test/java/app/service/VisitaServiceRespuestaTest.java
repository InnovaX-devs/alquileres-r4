package app.service;

import app.controller.dto.CambiarEstadoVisitaRequest;
import app.model.EstadoSolicitud;
import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Al aceptar o rechazar una visita se le avisa por email a quien la pidió. */
class VisitaServiceRespuestaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2030, 1, 10, 15, 30);

    private SolicitudVisitaRepository solicitudRepository;
    private EmailService emailService;
    private VisitaService service;
    private SolicitudVisita solicitud;
    private Usuario solicitante;

    @BeforeEach
    void setUp() {
        solicitudRepository = mock(SolicitudVisitaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        emailService = mock(EmailService.class);
        service = new VisitaService(solicitudRepository, mock(PropiedadRepository.class), usuarioRepository, emailService);

        Usuario publicador = new Usuario();
        publicador.setId(1L);
        publicador.setEmail("publicador@test.com");
        when(usuarioRepository.findByEmail("publicador@test.com")).thenReturn(Optional.of(publicador));

        solicitante = new Usuario();
        solicitante.setId(2L);
        solicitante.setEmail("solicitante@test.com");
        solicitante.setNombre("Juan");

        Propiedad propiedad = new Propiedad();
        propiedad.setId(10L);
        propiedad.setTitulo("Depto luminoso");
        propiedad.setCalle("Sobremonte");
        propiedad.setAltura("1250");
        propiedad.setPiso("3B");
        propiedad.setUsuario(publicador);

        solicitud = new SolicitudVisita();
        solicitud.setId(100L);
        solicitud.setUsuario(solicitante);
        solicitud.setPropiedad(propiedad);
        solicitud.setFechaPropuesta(FECHA);

        when(solicitudRepository.findById(100L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudVisita.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CambiarEstadoVisitaRequest request(EstadoSolicitud estado) {
        CambiarEstadoVisitaRequest r = new CambiarEstadoVisitaRequest();
        r.setEstado(estado);
        return r;
    }

    @Test
    void alAceptarSeLeAvisaAlSolicitante() {
        service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "publicador@test.com");

        verify(emailService).enviarRespuestaVisita(
                "solicitante@test.com", "Juan", "Depto luminoso", "Sobremonte 1250, piso 3B", FECHA, true);
    }

    @Test
    void alRechazarSeLeAvisaAlSolicitante() {
        service.cambiarEstado(100L, request(EstadoSolicitud.RECHAZADA), "publicador@test.com");

        verify(emailService).enviarRespuestaVisita(
                "solicitante@test.com", "Juan", "Depto luminoso", "Sobremonte 1250, piso 3B", FECHA, false);
    }

    @Test
    void siFallaElEmailElCambioDeEstadoSeGuardaIgual() {
        doThrow(new RuntimeException("SMTP caído")).when(emailService)
                .enviarRespuestaVisita(anyString(), any(), any(), any(), any(), anyBoolean());

        SolicitudVisita resultado = assertDoesNotThrow(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "publicador@test.com"));

        assertEquals(EstadoSolicitud.ACEPTADA, resultado.getEstado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void siElEstadoNoCambiaNoSeReenviaElEmail() {
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);

        service.cambiarEstado(100L, request(EstadoSolicitud.ACEPTADA), "publicador@test.com");

        verify(emailService, never()).enviarRespuestaVisita(any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void siElSolicitanteNoTieneEmailNoSeIntentaEnviar() {
        solicitante.setEmail(null);

        assertDoesNotThrow(() ->
                service.cambiarEstado(100L, request(EstadoSolicitud.RECHAZADA), "publicador@test.com"));

        verify(emailService, never()).enviarRespuestaVisita(any(), any(), any(), any(), any(), anyBoolean());
    }
}
