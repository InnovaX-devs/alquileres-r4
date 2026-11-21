package app.service;

import app.controller.dto.PropiedadDetalleResponse;
import app.model.ImagenPropiedad;
import app.model.Inmobiliaria;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.ImagenPropiedadRepository;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import app.storage.AlmacenamientoImagenes;
import app.storage.AlmacenamientoImagenesException;
import app.storage.ImagenAlmacenada;
import app.storage.ImagenesProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Issue #59: POST /propiedades/{id}/imagenes. */
class ImagenPropiedadServiceTest {

    private PropiedadRepository propiedadRepository;
    private UsuarioRepository usuarioRepository;
    private ImagenPropiedadRepository imagenRepository;
    private AlmacenamientoImagenes almacenamiento;
    private ImagenPropiedadService service;

    private Usuario publicador;
    private Propiedad propiedad;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        imagenRepository = mock(ImagenPropiedadRepository.class);
        almacenamiento = mock(AlmacenamientoImagenes.class);
        service = new ImagenPropiedadService(propiedadRepository, usuarioRepository, imagenRepository,
                almacenamiento, ImagenesProperties.porDefecto());

        publicador = usuario(1L, "pub@test.com", null);
        propiedad = new Propiedad();
        propiedad.setId(15L);
        propiedad.setUsuario(publicador);

        when(propiedadRepository.findById(15L)).thenReturn(Optional.of(propiedad));
        when(usuarioRepository.findByEmail("pub@test.com")).thenReturn(Optional.of(publicador));
        when(imagenRepository.save(any())).thenAnswer(inv -> {
            ImagenPropiedad i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });
    }

    private static Usuario usuario(Long id, String email, Inmobiliaria inmobiliaria) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setInmobiliaria(inmobiliaria);
        return u;
    }

    private static MockMultipartFile jpg() {
        return new MockMultipartFile("imagen", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    private static HttpStatus statusDe(ResponseStatusException e) {
        return HttpStatus.valueOf(e.getStatusCode().value());
    }

    @Test
    void elPublicadorSubeLaImagenYQuedaAlFinal() {
        when(imagenRepository.findMaxOrdenByPropiedadId(15L)).thenReturn(2);
        when(almacenamiento.subir(any(), eq("alquileres/propiedades/15")))
                .thenReturn(new ImagenAlmacenada("https://res.cloudinary.com/x/foto.jpg", "alquileres/propiedades/15/abc"));

        ImagenPropiedad guardada = service.subir(15L, jpg(), "pub@test.com");

        assertEquals("https://res.cloudinary.com/x/foto.jpg", guardada.getUrl());
        assertEquals("alquileres/propiedades/15/abc", guardada.getIdentificadorExterno());
        assertEquals(3, guardada.getOrden());
        assertSame(propiedad, guardada.getPropiedad());
    }

    @Test
    void unMiembroDeLaInmobiliariaPuedeSubir() {
        Inmobiliaria inmobiliaria = new Inmobiliaria();
        inmobiliaria.setId(7L);
        propiedad.setUsuario(null);
        propiedad.setInmobiliaria(inmobiliaria);
        when(usuarioRepository.findByEmail("colega@test.com"))
                .thenReturn(Optional.of(usuario(2L, "colega@test.com", inmobiliaria)));
        when(almacenamiento.subir(any(), anyString())).thenReturn(new ImagenAlmacenada("https://u", "id"));

        assertEquals(1, service.subir(15L, jpg(), "colega@test.com").getOrden());
    }

    @Test
    void otroUsuarioRecibe403YNoSeSubeNada() {
        when(usuarioRepository.findByEmail("otro@test.com"))
                .thenReturn(Optional.of(usuario(2L, "otro@test.com", null)));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.subir(15L, jpg(), "otro@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, statusDe(e));
        verifyNoInteractions(almacenamiento);
        verify(imagenRepository, never()).save(any());
    }

    @Test
    void propiedadInexistenteDevuelve404() {
        when(propiedadRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.subir(99L, jpg(), "pub@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, statusDe(e));
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void archivoVacioDevuelve400() {
        MockMultipartFile vacio = new MockMultipartFile("imagen", "f.jpg", "image/jpeg", new byte[0]);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.subir(15L, vacio, "pub@test.com"));

        assertEquals(HttpStatus.BAD_REQUEST, statusDe(e));
    }

    @Test
    void formatoNoPermitidoDevuelve415() {
        MockMultipartFile pdf = new MockMultipartFile("imagen", "f.pdf", "application/pdf", new byte[]{1});

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.subir(15L, pdf, "pub@test.com"));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusDe(e));
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void superarElMaximoDeImagenesDevuelve409() {
        when(imagenRepository.countByPropiedadId(15L)).thenReturn(20L);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.subir(15L, jpg(), "pub@test.com"));

        assertEquals(HttpStatus.CONFLICT, statusDe(e));
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void falloDelProveedorDevuelve502() {
        when(almacenamiento.subir(any(), anyString()))
                .thenThrow(new AlmacenamientoImagenesException("caído", new RuntimeException()));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.subir(15L, jpg(), "pub@test.com"));

        assertEquals(HttpStatus.BAD_GATEWAY, statusDe(e));
        verify(imagenRepository, never()).save(any());
    }

    @Test
    void siFallaLaBaseSeBorraLaImagenSubida() {
        when(almacenamiento.subir(any(), anyString())).thenReturn(new ImagenAlmacenada("https://u", "id-huerfano"));
        doThrow(new RuntimeException("db caída")).when(imagenRepository).save(any());

        assertThrows(RuntimeException.class, () -> service.subir(15L, jpg(), "pub@test.com"));

        verify(almacenamiento).eliminar("id-huerfano");
    }

    @Test
    void elDetalleDevuelveLasImagenesOrdenadas() {
        List<ImagenPropiedad> imagenes = new ArrayList<>();
        imagenes.add(imagen(3L, 2, "https://b"));
        imagenes.add(imagen(1L, 3, "https://c"));
        imagenes.add(imagen(2L, 1, "https://a"));
        propiedad.setImagenes(imagenes);

        PropiedadDetalleResponse detalle = PropiedadDetalleResponse.from(propiedad, null);

        assertEquals(List.of("https://a", "https://b", "https://c"), detalle.getImagenes());
    }

    private static ImagenPropiedad imagen(Long id, int orden, String url) {
        ImagenPropiedad i = new ImagenPropiedad();
        i.setId(id);
        i.setOrden(orden);
        i.setUrl(url);
        return i;
    }
}
