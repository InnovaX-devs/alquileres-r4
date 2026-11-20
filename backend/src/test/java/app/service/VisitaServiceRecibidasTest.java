package app.service;

import app.model.Inmobiliaria;
import app.model.SolicitudVisita;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Issue #57: el publicador ve las solicitudes de sus propiedades (o las de su inmobiliaria). */
class VisitaServiceRecibidasTest {

    private SolicitudVisitaRepository solicitudRepository;
    private UsuarioRepository usuarioRepository;
    private VisitaService service;

    @BeforeEach
    void setUp() {
        solicitudRepository = mock(SolicitudVisitaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new VisitaService(solicitudRepository, mock(PropiedadRepository.class),
                usuarioRepository, mock(EmailService.class));
    }

    @Test
    void publicadorIndependienteVeSoloLasSolicitudesDeSusPropiedades() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("publicador@test.com");
        when(usuarioRepository.findByEmail("publicador@test.com")).thenReturn(Optional.of(usuario));

        List<SolicitudVisita> esperadas = List.of(new SolicitudVisita());
        when(solicitudRepository.findRecibidasPorUsuario(1L)).thenReturn(esperadas);

        assertEquals(esperadas, service.listarRecibidas("publicador@test.com"));
        verify(solicitudRepository, never()).findRecibidasPorUsuarioOInmobiliaria(1L, 5L);
    }

    @Test
    void miembroDeInmobiliariaVeTambienLasSolicitudesDeLaInmobiliaria() {
        Inmobiliaria inmobiliaria = new Inmobiliaria();
        inmobiliaria.setId(5L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("agente@test.com");
        usuario.setInmobiliaria(inmobiliaria);
        when(usuarioRepository.findByEmail("agente@test.com")).thenReturn(Optional.of(usuario));

        List<SolicitudVisita> esperadas = List.of(new SolicitudVisita());
        when(solicitudRepository.findRecibidasPorUsuarioOInmobiliaria(1L, 5L)).thenReturn(esperadas);

        assertEquals(esperadas, service.listarRecibidas("agente@test.com"));
    }
}
