package app.service;

import app.model.EstadoPropiedad;
import app.model.ImagenPropiedad;
import app.model.Moneda;
import app.model.Propiedad;
import app.repository.PropiedadRepository;
import app.service.CompartirPropiedadService.VistaPrevia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Issue #151: datos de la vista previa al compartir una propiedad. */
class CompartirPropiedadServiceTest {

    private PropiedadRepository propiedadRepository;
    private CompartirPropiedadService service;

    @BeforeEach
    void setUp() {
        propiedadRepository = mock(PropiedadRepository.class);
        // Con barra final a propósito: no tiene que quedar doble
        service = new CompartirPropiedadService(propiedadRepository, "https://alkilea.vercel.app/");
    }

    private Propiedad propiedad() {
        Propiedad p = new Propiedad();
        p.setId(7L);
        p.setTitulo("Depto luminoso cerca del río");
        p.setPrecio(new BigDecimal("350000"));
        p.setMoneda(Moneda.ARS);
        p.setCalle("Sobremonte");
        p.setAltura("1234");
        p.setPiso("2B");
        p.setHabitaciones(2);
        p.setMetrosCuadrados(55);
        p.setImagenes(new ArrayList<>());
        return p;
    }

    private ImagenPropiedad imagen(long id, int orden, String url) {
        ImagenPropiedad i = new ImagenPropiedad();
        i.setId(id);
        i.setOrden(orden);
        i.setUrl(url);
        return i;
    }

    @Test
    void armaTituloDescripcionYDestino() {
        VistaPrevia vista = service.armar(propiedad());

        assertEquals("Depto luminoso cerca del río", vista.titulo());
        assertEquals("$ 350.000 por mes · Sobremonte 1234, piso 2B · 2 habitaciones · 55 m²", vista.descripcion());
        assertEquals("https://alkilea.vercel.app/propiedades/7", vista.urlDestino());
    }

    @Test
    void sinFotosUsaLaImagenGenericaConLaMarca() {
        VistaPrevia vista = service.armar(propiedad());

        assertEquals("https://alkilea.vercel.app/compartir-alkilea.png", vista.imagen());
        assertTrue(vista.conMedidas());
    }

    @Test
    void usaLaPrimeraFotoSegunElOrden() {
        Propiedad p = propiedad();
        p.setImagenes(List.of(
                imagen(1, 2, "https://otro.host/segunda.jpg"),
                imagen(2, 0, "https://otro.host/primera.jpg")));

        VistaPrevia vista = service.armar(p);

        assertEquals("https://otro.host/primera.jpg", vista.imagen());
        assertFalse(vista.conMedidas(), "una foto que no es de Cloudinary no tiene medida conocida");
    }

    @Test
    void lasFotosDeCloudinarySePidenEn1200x630() {
        Propiedad p = propiedad();
        p.setImagenes(List.of(imagen(1, 0,
                "https://res.cloudinary.com/demo/image/upload/v1712/propiedades/abc.png")));

        VistaPrevia vista = service.armar(p);

        assertEquals("https://res.cloudinary.com/demo/image/upload/"
                + "c_fill,g_auto,w_1200,h_630,q_auto,f_jpg/v1712/propiedades/abc.png", vista.imagen());
        assertTrue(vista.conMedidas());
    }

    @Test
    void sinTituloUsaLaDireccion() {
        Propiedad p = propiedad();
        p.setTitulo("  ");

        assertEquals("Sobremonte 1234, piso 2B", service.armar(p).titulo());
    }

    @Test
    void sinTituloNiDireccionTieneUnTituloGenerico() {
        Propiedad p = propiedad();
        p.setTitulo(null);
        p.setCalle(null);
        p.setAltura(null);

        VistaPrevia vista = service.armar(p);

        assertEquals("Propiedad en alquiler", vista.titulo());
        assertEquals("$ 350.000 por mes · 2 habitaciones · 55 m²", vista.descripcion());
    }

    @Test
    void avisaSiYaEstaAlquilada() {
        Propiedad p = propiedad();
        p.setEstado(EstadoPropiedad.ALQUILADA);

        assertTrue(service.armar(p).descripcion().startsWith("Ya alquilada · $ 350.000"));
    }

    @Test
    void formatoDePrecioIgualAlDelFront() {
        assertEquals("$ 350.000", CompartirPropiedadService.formatearPrecio(new BigDecimal("350000.00"), Moneda.ARS));
        assertEquals("US$ 1.200,50", CompartirPropiedadService.formatearPrecio(new BigDecimal("1200.5"), Moneda.USD));
        assertEquals("Consultar precio", CompartirPropiedadService.formatearPrecio(null, Moneda.ARS));
        assertEquals("Monoambiente", CompartirPropiedadService.formatearHabitaciones(0));
        assertEquals("1 habitación", CompartirPropiedadService.formatearHabitaciones(1));
    }

    @Test
    void propiedadInexistenteDevuelveVacio() {
        when(propiedadRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.vistaPrevia(99L).isEmpty());
        assertEquals("https://alkilea.vercel.app/propiedades/99", service.urlDetalle(99L));
    }
}
