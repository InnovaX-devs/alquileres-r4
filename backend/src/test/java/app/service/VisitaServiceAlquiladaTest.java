package app.service;

import app.controller.dto.SolicitudVisitaRequest;
import app.model.EstadoPropiedad;
import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Una propiedad alquilada no recibe nuevos pedidos de visita. */
class VisitaServiceAlquiladaTest {

    private SolicitudVisitaRepository solicitudRepository;
    private VisitaService service;
    private Propiedad propiedad;

    @BeforeEach
    void setUp() {
        solicitudRepository = mock(SolicitudVisitaRepository.class);
        PropiedadRepository propiedadRepository = mock(PropiedadRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        service = new VisitaService(solicitudRepository, propiedadRepository, usuarioRepository,
                mock(EmailService.class));

        Usuario publicador = new Usuario();
        publicador.setId(1L);
        publicador.setEmail("pub@test.com");

        Usuario interesado = new Usuario();
        interesado.setId(2L);
        interesado.setEmail("interesado@test.com");
        when(usuarioRepository.findByEmail("interesado@test.com")).thenReturn(Optional.of(interesado));

        propiedad = new Propiedad();
        propiedad.setId(10L);
        propiedad.setUsuario(publicador);
        when(propiedadRepository.findById(10L)).thenReturn(Optional.of(propiedad));
        when(solicitudRepository.save(any(SolicitudVisita.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static SolicitudVisitaRequest request() {
        SolicitudVisitaRequest r = new SolicitudVisitaRequest();
        r.setPropiedadId(10L);
        r.setFechaPropuesta(LocalDateTime.now().plusDays(2));
        return r;
    }

    @Test
    void noSePuedePedirVisitaAUnaAlquilada() {
        propiedad.setEstado(EstadoPropiedad.ALQUILADA);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.solicitarVisita(request(), "interesado@test.com"));

        assertEquals(409, e.getStatusCode().value());
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void unaDisponibleSiRecibeElPedido() {
        propiedad.setEstado(EstadoPropiedad.DISPONIBLE);

        service.solicitarVisita(request(), "interesado@test.com");

        verify(solicitudRepository).save(any(SolicitudVisita.class));
    }
}
