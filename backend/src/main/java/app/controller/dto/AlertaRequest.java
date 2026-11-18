package app.controller.dto;

import app.model.Moneda;
import java.math.BigDecimal;

public class AlertaRequest {

    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private Moneda moneda;
    private Integer habitacionesMin;
    private String calle;

    public BigDecimal getPrecioMin() { return precioMin; }
    public void setPrecioMin(BigDecimal precioMin) { this.precioMin = precioMin; }

    public BigDecimal getPrecioMax() { return precioMax; }
    public void setPrecioMax(BigDecimal precioMax) { this.precioMax = precioMax; }

    public Moneda getMoneda() { return moneda; }
    public void setMoneda(Moneda moneda) { this.moneda = moneda; }

    public Integer getHabitacionesMin() { return habitacionesMin; }
    public void setHabitacionesMin(Integer habitacionesMin) { this.habitacionesMin = habitacionesMin; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
}