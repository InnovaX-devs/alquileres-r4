package app.service;

import app.model.FavoritoInmobiliaria;
import app.model.FavoritoPropiedad;
import app.model.Inmobiliaria;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.FavoritoInmobiliariaRepository;
import app.repository.FavoritoPropiedadRepository;
import app.repository.InmobiliariaRepository;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Quitar favoritos, usado por el botón de corazón (#82 y #83). */
class FavoritoServiceQuitarTest {

    private FavoritoPropiedadRepository favoritoPropiedadRepository;
    private FavoritoInmobiliariaRepository favoritoInmobiliariaRepository;
    private FavoritoService service;

    private Usuario usuario;
    private Propiedad propiedad;
    private Inmobiliaria inmobiliaria;

    @BeforeEach
    void setUp() {
        favoritoPropiedadRepository = mock(FavoritoPropiedadRepository.class);
        favoritoInmobiliariaRepository = mock(FavoritoInmobiliariaRepository.class);
        PropiedadRepository propiedadRepository = mock(PropiedadRepository.class);
        InmobiliariaRepository inmobiliariaRepository = mock(InmobiliariaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        service = new FavoritoService(favoritoPropiedadRepository, favoritoInmobiliariaRepository,
                propiedadRepository, inmobiliariaRepository, usuarioRepository);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@test.com");
        propiedad = new Propiedad();
        propiedad.setId(10L);
        inmobiliaria = new Inmobiliaria();
        inmobiliaria.setId(5L);

        when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(usuario));
        when(propiedadRepository.findById(10L)).thenReturn(Optional.of(propiedad));
        when(inmobiliariaRepository.findById(5L)).thenReturn(Optional.of(inmobiliaria));
    }

    @Test
    void quitaLaPropiedadFavorita() {
        FavoritoPropiedad favorito = new FavoritoPropiedad();
        when(favoritoPropiedadRepository.findByUsuarioAndPropiedad(usuario, propiedad)).thenReturn(Optional.of(favorito));

        service.quitarPropiedadFavorita(10L, "usuario@test.com");

        verify(favoritoPropiedadRepository).delete(favorito);
    }

    @Test
    void propiedadQueNoEraFavoritaDevuelve404() {
        when(favoritoPropiedadRepository.findByUsuarioAndPropiedad(usuario, propiedad)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.quitarPropiedadFavorita(10L, "usuario@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        verify(favoritoPropiedadRepository, never()).delete(any());
    }

    @Test
    void quitaLaInmobiliariaFavorita() {
        FavoritoInmobiliaria favorito = new FavoritoInmobiliaria();
        when(favoritoInmobiliariaRepository.findByUsuarioAndInmobiliaria(usuario, inmobiliaria)).thenReturn(Optional.of(favorito));

        service.quitarInmobiliariaFavorita(5L, "usuario@test.com");

        verify(favoritoInmobiliariaRepository).delete(favorito);
    }

    @Test
    void inmobiliariaQueNoEraFavoritaDevuelve404() {
        when(favoritoInmobiliariaRepository.findByUsuarioAndInmobiliaria(usuario, inmobiliaria)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.quitarInmobiliariaFavorita(5L, "usuario@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        verify(favoritoInmobiliariaRepository, never()).delete(any());
    }
}
