package app.controller.dto;

import app.model.Alerta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertaResponse {

    private Long id;
    private Boolean activa;
    private LocalDateTime creadaEn;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private String moneda;
    private Integer habitacionesMin;
    private String calle;

    public static AlertaResponse from(Alerta alerta) {
        AlertaResponse dto = new AlertaResponse();
        dto.id = alerta.getId();
        dto.activa = alerta.getActiva();
        dto.creadaEn = alerta.getCreadaEn();
        dto.precioMin = alerta.getPrecioMin();
        dto.precioMax = alerta.getPrecioMax();
        dto.moneda = alerta.getMoneda() != null ? alerta.getMoneda().name() : null;
        dto.habitacionesMin = alerta.getHabitacionesMin();
        dto.calle = alerta.getCalle();
        return dto;
    }

    public Long getId() { return id; }
    public Boolean getActiva() { return activa; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public BigDecimal getPrecioMin() { return precioMin; }
    public BigDecimal getPrecioMax() { return precioMax; }
    public String getMoneda() { return moneda; }
    public Integer getHabitacionesMin() { return habitacionesMin; }
    public String getCalle() { return calle; }
}
