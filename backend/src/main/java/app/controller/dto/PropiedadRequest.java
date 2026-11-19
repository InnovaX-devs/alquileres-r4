package app.controller.dto;

import app.model.Moneda;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class PropiedadRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede superar los 255 caracteres")
    private String titulo;
    private String descripcion;
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;
    @DecimalMin(value = "0.0", message = "Las expensas no pueden ser negativas")
    private BigDecimal expensas;
    @NotNull(message = "La moneda es obligatoria")
    private Moneda moneda;
    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 255, message = "La calle no puede superar los 255 caracteres")
    private String calle;
    @NotBlank(message = "La altura es obligatoria")
    @Size(max = 255, message = "La altura no puede superar los 255 caracteres")
    private String altura;
    private String piso;
    @Min(value = 0, message = "Las habitaciones no pueden ser negativas")
    private Integer habitaciones;
    @Min(value = 1, message = "Los metros cuadrados deben ser mayores a 0")
    private Integer metrosCuadrados;
    private Boolean incluyeImpuestos;
    @Min(value = 1, message = "El plazo de actualización debe ser de al menos 1 mes")
    private Integer plazoActualizacionMeses;
    private String requisitosIngreso;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getExpensas() { return expensas; }
    public void setExpensas(BigDecimal expensas) { this.expensas = expensas; }

    public Moneda getMoneda() { return moneda; }
    public void setMoneda(Moneda moneda) { this.moneda = moneda; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getAltura() { return altura; }
    public void setAltura(String altura) { this.altura = altura; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }

    public Integer getHabitaciones() { return habitaciones; }
    public void setHabitaciones(Integer habitaciones) { this.habitaciones = habitaciones; }

    public Integer getMetrosCuadrados() { return metrosCuadrados; }
    public void setMetrosCuadrados(Integer metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    public Boolean getIncluyeImpuestos() { return incluyeImpuestos; }
    public void setIncluyeImpuestos(Boolean incluyeImpuestos) { this.incluyeImpuestos = incluyeImpuestos; }

    public Integer getPlazoActualizacionMeses() { return plazoActualizacionMeses; }
    public void setPlazoActualizacionMeses(Integer plazoActualizacionMeses) { this.plazoActualizacionMeses = plazoActualizacionMeses; }

    public String getRequisitosIngreso() { return requisitosIngreso; }
    public void setRequisitosIngreso(String requisitosIngreso) { this.requisitosIngreso = requisitosIngreso; }
}
