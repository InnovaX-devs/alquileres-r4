package app.controller.dto;

import app.model.Moneda;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public class AlertaRequest {

    @DecimalMin(value = "0.0", message = "El precio mínimo no puede ser negativo")
    private BigDecimal precioMin;
    @DecimalMin(value = "0.0", message = "El precio máximo no puede ser negativo")
    private BigDecimal precioMax;
    private Moneda moneda;
    @Min(value = 1, message = "Las habitaciones mínimas deben ser al menos 1")
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

    // ── Validaciones cruzadas (issue #48) ────────────────────────────────────

    /** Criterios mínimos: al menos uno entre precio (min/max), habitaciones mínimas o calle. La moneda sola no alcanza. */
    @JsonIgnore
    @AssertTrue(message = "Debe indicar al menos un criterio: precio mínimo, precio máximo, habitaciones mínimas o calle")
    public boolean isCriterios() {
        return precioMin != null
                || precioMax != null
                || habitacionesMin != null
                || (calle != null && !calle.isBlank());
    }

    /** Un precio sin moneda es ambiguo (no se sabe si son pesos o dólares). */
    @JsonIgnore
    @AssertTrue(message = "La moneda es obligatoria cuando se indica un precio mínimo o máximo")
    public boolean isMonedaConPrecio() {
        return (precioMin == null && precioMax == null) || moneda != null;
    }

    @JsonIgnore
    @AssertTrue(message = "El precio mínimo no puede ser mayor al precio máximo")
    public boolean isRangoPrecioValido() {
        return precioMin == null || precioMax == null || precioMin.compareTo(precioMax) <= 0;
    }
}
