package app.controller.dto;

import java.math.BigDecimal;

import app.geo.RioCuarto;
import app.model.ImagenPropiedad;
import app.model.Propiedad;
import java.util.Comparator;
import java.util.List;

public class PropiedadDetalleResponse {
    /** Las imágenes ya vienen ordenadas por @OrderBy; se reordena por si la lista se armó en memoria. */
    private static final Comparator<ImagenPropiedad> ORDEN_IMAGENES = Comparator
            .comparing(ImagenPropiedad::getOrden, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(ImagenPropiedad::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal expensas;
    private String moneda;
    private String calle;
    private String altura;
    private String piso;
    private Integer habitaciones;
    private Integer metrosCuadrados;
    private Boolean incluyeImpuestos;
    private Integer plazoActualizacionMeses;
    private String requisitosIngreso;
    private ContactoPublicadorResponse contacto; // null si no está verificado
    private List<String> imagenes;
    // Ubicación (issue #140). En público es aproximada: el centro de una celda de ~170 m, con
    // radioMetros para dibujar el círculo. Para el publicador es la exacta y radioMetros es null.
    private Double latitud;
    private Double longitud;
    private Integer radioMetros;

    /** Versión pública (listado y detalle): la ubicación va aproximada para no revelar la casa. */
    public static PropiedadDetalleResponse from(Propiedad propiedad, ContactoPublicadorResponse contacto) {
        PropiedadDetalleResponse dto = base(propiedad, contacto);
        if (propiedad.getLatitud() != null && propiedad.getLongitud() != null) {
            dto.latitud = RioCuarto.aproximarLatitud(propiedad.getLatitud());
            dto.longitud = RioCuarto.aproximarLongitud(propiedad.getLongitud());
            dto.radioMetros = RioCuarto.RADIO_APROXIMADO_METROS;
        }
        return dto;
    }

    /** Versión para quien publicó (al publicar y en /propiedades/mias): ubicación exacta. */
    public static PropiedadDetalleResponse paraPublicador(Propiedad propiedad) {
        PropiedadDetalleResponse dto = base(propiedad, null);
        dto.latitud = propiedad.getLatitud();
        dto.longitud = propiedad.getLongitud();
        return dto;
    }

    private static PropiedadDetalleResponse base(Propiedad propiedad, ContactoPublicadorResponse contacto) {
        PropiedadDetalleResponse dto = new PropiedadDetalleResponse();
        dto.id = propiedad.getId();
        dto.titulo = propiedad.getTitulo();
        dto.descripcion = propiedad.getDescripcion();
        dto.precio = propiedad.getPrecio();
        dto.expensas = propiedad.getExpensas();
        dto.moneda = propiedad.getMoneda() != null ? propiedad.getMoneda().name() : null;
        dto.calle = propiedad.getCalle();
        dto.altura = propiedad.getAltura();
        dto.piso = propiedad.getPiso();
        dto.habitaciones = propiedad.getHabitaciones();
        dto.metrosCuadrados = propiedad.getMetrosCuadrados();
        dto.incluyeImpuestos = propiedad.getIncluyeImpuestos();
        dto.plazoActualizacionMeses = propiedad.getPlazoActualizacionMeses();
        dto.requisitosIngreso = propiedad.getRequisitosIngreso();
        dto.contacto = contacto;
        dto.imagenes = propiedad.getImagenes() != null
            ? propiedad.getImagenes().stream()
                .sorted(ORDEN_IMAGENES)
                .map(ImagenPropiedad::getUrl)
                .toList()
            : List.of();
        return dto;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getExpensas() { return expensas; }
    public String getMoneda() { return moneda; }
    public String getCalle() { return calle; }
    public String getAltura() { return altura; }
    public String getPiso() { return piso; }
    public Integer getHabitaciones() { return habitaciones; }
    public Integer getMetrosCuadrados() { return metrosCuadrados; }
    public Boolean getIncluyeImpuestos() { return incluyeImpuestos; }
    public Integer getPlazoActualizacionMeses() { return plazoActualizacionMeses; }
    public String getRequisitosIngreso() { return requisitosIngreso; }
    public ContactoPublicadorResponse getContacto() { return contacto; }
    public List<String> getImagenes() { return imagenes; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public Integer getRadioMetros() { return radioMetros; }
}