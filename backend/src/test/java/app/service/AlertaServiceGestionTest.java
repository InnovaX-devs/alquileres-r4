package app.service;

import app.model.Alerta;
import app.model.Usuario;
import app.repository.AlertaRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Issue #60: listar, activar/desactivar y eliminar alertas; solo el dueño puede modificarlas. */
class AlertaServiceGestionTest {

    private AlertaRepository alertaRepository;
    private AlertaService service;

    private Usuario duenio;
    private Usuario otro;
    private Alerta alerta;

    @BeforeEach
    void setUp() {
        alertaRepository = mock(AlertaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        service = new AlertaService(alertaRepository, usuarioRepository, mock(EmailService.class));

        duenio = usuario(usuarioRepository, 1L, "duenio@test.com");
        otro = usuario(usuarioRepository, 2L, "otro@test.com");

        alerta = new Alerta();
        alerta.setId(10L);
        alerta.setUsuario(duenio);

        when(alertaRepository.findById(10L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.findById(99L)).thenReturn(Optional.empty());
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario usuario(UsuarioRepository repo, Long id, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        when(repo.findByEmail(email)).thenReturn(Optional.of(u));
        return u;
    }

    private void assertEstado(HttpStatus esperado, ResponseStatusException ex) {
        assertEquals(esperado, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void listaSoloLasAlertasDelUsuario() {
        when(alertaRepository.findByUsuarioIdOrderByCreadaEnDesc(1L)).thenReturn(List.of(alerta));

        assertEquals(List.of(alerta), service.listarAlertas("duenio@test.com"));
        verify(alertaRepository, never()).findAll();
    }

    @Test
    void elDuenioPuedeDesactivarLaAlerta() {
        Alerta actualizada = service.cambiarEstado(10L, false, "duenio@test.com");

        assertFalse(actualizada.getActiva());
    }

    @Test
    void cambiarEstadoDeAlertaInexistenteDa404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(99L, false, "duenio@test.com"));

        assertEstado(HttpStatus.NOT_FOUND, ex);
    }

    @Test
    void cambiarEstadoDeAlertaAjenaDa403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(10L, false, "otro@test.com"));

        assertEstado(HttpStatus.FORBIDDEN, ex);
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void elDuenioPuedeEliminarLaAlerta() {
        service.eliminarAlerta(10L, "duenio@test.com");

        verify(alertaRepository).delete(alerta);
    }

    @Test
    void eliminarAlertaInexistenteDa404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.eliminarAlerta(99L, "duenio@test.com"));

        assertEstado(HttpStatus.NOT_FOUND, ex);
    }

    @Test
    void eliminarAlertaAjenaDa403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.eliminarAlerta(10L, "otro@test.com"));

        assertEstado(HttpStatus.FORBIDDEN, ex);
        verify(alertaRepository, never()).delete(any(Alerta.class));
    }
}
