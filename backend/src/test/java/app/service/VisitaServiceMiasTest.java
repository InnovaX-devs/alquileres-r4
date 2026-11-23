package app.service;

import app.model.SolicitudVisita;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Issue #56: el usuario ve solo las solicitudes de visita que realizó. */
class VisitaServiceMiasTest {

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
    void devuelveLasSolicitudesDelUsuarioAutenticado() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("inquilino@test.com");
        when(usuarioRepository.findByEmail("inquilino@test.com")).thenReturn(Optional.of(usuario));

        List<SolicitudVisita> esperadas = List.of(new SolicitudVisita());
        when(solicitudRepository.findRealizadasPorUsuario(1L)).thenReturn(esperadas);

        assertEquals(esperadas, service.listarMias("inquilino@test.com"));
    }

    @Test
    void usuarioInexistenteDevuelve401() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listarMias("nadie@test.com"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verifyNoInteractions(solicitudRepository);
    }
}
