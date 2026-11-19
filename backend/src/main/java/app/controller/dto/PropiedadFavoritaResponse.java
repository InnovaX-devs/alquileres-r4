package app.controller.dto;

import app.model.FavoritoPropiedad;
import app.model.ImagenPropiedad;
import app.model.Moneda;
import app.model.Propiedad;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class PropiedadFavoritaResponse {

    private Long favoritoId;
    private Long propiedadId;
    private String titulo;
    private BigDecimal precio;
    private BigDecimal expensas;
    private Moneda moneda;
    private String calle;
    private String altura;
    private Integer habitaciones;
    private Integer metrosCuadrados;
    private String imagenPrincipal;
    private LocalDateTime fechaGuardado;

    public static PropiedadFavoritaResponse from(FavoritoPropiedad favorito) {
        Propiedad propiedad = favorito.getPropiedad();
        PropiedadFavoritaResponse dto = new PropiedadFavoritaResponse();
        dto.favoritoId = favorito.getId();
        dto.propiedadId = propiedad.getId();
        dto.titulo = propiedad.getTitulo();
        dto.precio = propiedad.getPrecio();
        dto.expensas = propiedad.getExpensas();
        dto.moneda = propiedad.getMoneda();
        dto.calle = propiedad.getCalle();
        dto.altura = propiedad.getAltura();
        dto.habitaciones = propiedad.getHabitaciones();
        dto.metrosCuadrados = propiedad.getMetrosCuadrados();
        dto.imagenPrincipal = propiedad.getImagenes() == null ? null : propiedad.getImagenes().stream()
                .min(Comparator.comparing(ImagenPropiedad::getOrden, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ImagenPropiedad::getUrl)
                .filter(Objects::nonNull)
                .orElse(null);
        dto.fechaGuardado = favorito.getFechaGuardado();
        return dto;
    }

    public Long getFavoritoId() { return favoritoId; }
    public Long getPropiedadId() { return propiedadId; }
    public String getTitulo() { return titulo; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getExpensas() { return expensas; }
    public Moneda getMoneda() { return moneda; }
    public String getCalle() { return calle; }
    public String getAltura() { return altura; }
    public Integer getHabitaciones() { return habitaciones; }
    public Integer getMetrosCuadrados() { return metrosCuadrados; }
    public String getImagenPrincipal() { return imagenPrincipal; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
}
