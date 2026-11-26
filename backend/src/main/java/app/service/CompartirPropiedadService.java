package app.service;

import app.model.EstadoPropiedad;
import app.model.ImagenPropiedad;
import app.model.Moneda;
import app.model.Propiedad;
import app.repository.PropiedadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Datos de la vista previa al compartir una propiedad por WhatsApp (issue #151).
 *
 * WhatsApp no ejecuta JavaScript: para armar la tarjeta lee las etiquetas Open Graph del HTML que
 * recibe. Como el front es una SPA, ese HTML lo arma el back (ver CompartirController) con lo que
 * sale de acá: título, descripción (precio y dirección), foto y la URL del detalle en el front.
 *
 * Los textos siguen el mismo formato que el front (utils/propiedad.js) para que la tarjeta de
 * WhatsApp diga lo mismo que la página.
 */
@Service
public class CompartirPropiedadService {

    /** Imagen con la marca para las propiedades sin fotos. Vive en frontend/public. */
    static final String IMAGEN_GENERICA = "/compartir-alkilea.png";
    static final int ANCHO_IMAGEN = 1200;
    static final int ALTO_IMAGEN = 630;

    private static final Comparator<ImagenPropiedad> ORDEN_IMAGENES = Comparator
            .comparing(ImagenPropiedad::getOrden, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(ImagenPropiedad::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final PropiedadRepository propiedadRepository;
    private final String urlFront;

    public CompartirPropiedadService(PropiedadRepository propiedadRepository,
                                     @Value("${app.frontend-url:http://localhost:5173}") String urlFront) {
        this.propiedadRepository = propiedadRepository;
        this.urlFront = sinBarraFinal(urlFront.trim());
    }

    /**
     * @param titulo       og:title
     * @param descripcion  og:description: precio, dirección y ambientes
     * @param imagen       og:image, siempre una URL absoluta
     * @param conMedidas   la imagen es de 1200x630 (se pueden informar og:image:width/height)
     * @param urlDestino   detalle de la propiedad en el front, adonde se redirige al navegador
     */
    public record VistaPrevia(String titulo, String descripcion, String imagen, boolean conMedidas,
                              String urlDestino) {
    }

    /** La vista previa de la propiedad, o vacío si no existe. */
    @Transactional(readOnly = true) // las imágenes son lazy y open-in-view está apagado
    public Optional<VistaPrevia> vistaPrevia(Long id) {
        return propiedadRepository.findById(id).map(this::armar);
    }

    /** Adonde mandar al navegador cuando la propiedad no existe: el front muestra su 404. */
    public String urlDetalle(Long id) {
        return urlFront + "/propiedades/" + id;
    }

    VistaPrevia armar(Propiedad propiedad) {
        String ubicacion = formatearUbicacion(propiedad);
        String titulo = noVacio(propiedad.getTitulo()) ? propiedad.getTitulo().trim()
                : ubicacion != null ? ubicacion : "Propiedad en alquiler";

        List<String> partes = new ArrayList<>();
        String precio = formatearPrecio(propiedad.getPrecio(), propiedad.getMoneda());
        partes.add(propiedad.getPrecio() != null ? precio + " por mes" : precio);
        if (ubicacion != null) partes.add(ubicacion);
        String habitaciones = formatearHabitaciones(propiedad.getHabitaciones());
        if (habitaciones != null) partes.add(habitaciones);
        if (propiedad.getMetrosCuadrados() != null) partes.add(propiedad.getMetrosCuadrados() + " m²");

        String descripcion = String.join(" · ", partes);
        if (propiedad.getEstado() == EstadoPropiedad.ALQUILADA) {
            descripcion = "Ya alquilada · " + descripcion;
        }

        String foto = primeraFoto(propiedad);
        String imagen = foto != null ? ajustarFoto(foto) : urlFront + IMAGEN_GENERICA;
        boolean conMedidas = foto == null || !imagen.equals(foto);

        return new VistaPrevia(titulo, descripcion, imagen, conMedidas, urlDetalle(propiedad.getId()));
    }

    private static String primeraFoto(Propiedad propiedad) {
        if (propiedad.getImagenes() == null) return null;
        return propiedad.getImagenes().stream()
                .sorted(ORDEN_IMAGENES)
                .map(ImagenPropiedad::getUrl)
                .filter(Objects::nonNull)
                .filter(url -> url.startsWith("http://") || url.startsWith("https://"))
                .findFirst()
                .orElse(null);
    }

    /**
     * Si la foto está en Cloudinary, pide una versión de 1200x630 en JPG: es la medida que WhatsApp
     * muestra grande, y una foto original de varios MB a veces no la carga. JPG y no f_auto, porque
     * f_auto puede devolver AVIF/WebP y no todos los clientes de WhatsApp los muestran.
     * Otras URLs quedan como están.
     */
    static String ajustarFoto(String url) {
        String marca = "/image/upload/";
        int i = url.indexOf(marca);
        if (!url.contains("res.cloudinary.com") || i < 0) return url;
        int corte = i + marca.length();
        return url.substring(0, corte)
                + "c_fill,g_auto,w_" + ANCHO_IMAGEN + ",h_" + ALTO_IMAGEN + ",q_auto,f_jpg/"
                + url.substring(corte);
    }

    // ── Formato (igual que frontend/src/utils/propiedad.js) ──────────────────

    /** 350000, ARS → "$ 350.000" · 1200.5, USD → "US$ 1.200,50" · sin precio → "Consultar precio" */
    static String formatearPrecio(BigDecimal precio, Moneda moneda) {
        if (precio == null) return "Consultar precio";
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-AR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        boolean entero = precio.stripTrailingZeros().scale() <= 0;
        DecimalFormat formato = new DecimalFormat(entero ? "#,##0" : "#,##0.00", simbolos);
        String simbolo = moneda == Moneda.USD ? "US$" : moneda == Moneda.ARS ? "$" : "";
        return (simbolo + " " + formato.format(precio)).trim();
    }

    /** "Av. Colón 1234, piso 5B" · sin calle ni altura → null */
    static String formatearUbicacion(Propiedad propiedad) {
        List<String> partes = new ArrayList<>();
        if (noVacio(propiedad.getCalle())) partes.add(propiedad.getCalle().trim());
        if (noVacio(propiedad.getAltura())) partes.add(propiedad.getAltura().trim());
        if (partes.isEmpty()) return null;
        String direccion = String.join(" ", partes);
        return noVacio(propiedad.getPiso()) ? direccion + ", piso " + propiedad.getPiso().trim() : direccion;
    }

    /** 0 → "Monoambiente" · 1 → "1 habitación" · 3 → "3 habitaciones" · sin dato → null */
    static String formatearHabitaciones(Integer habitaciones) {
        if (habitaciones == null) return null;
        if (habitaciones == 0) return "Monoambiente";
        return habitaciones + (habitaciones == 1 ? " habitación" : " habitaciones");
    }

    private static boolean noVacio(String texto) {
        return texto != null && !texto.isBlank();
    }

    private static String sinBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
