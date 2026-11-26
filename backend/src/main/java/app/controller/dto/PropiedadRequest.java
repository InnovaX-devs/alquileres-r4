package app.controller.dto;

import app.geo.RioCuarto;
import app.model.Moneda;
import app.model.Propiedad;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
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

    // Ubicación confirmada con el pin del mapa (issue #140). Opcional.
    private Double latitud;
    private Double longitud;

    /** La ubicación es opcional, pero si viene tienen que estar las dos coordenadas y caer en Río Cuarto. */
    @JsonIgnore
    @AssertTrue(message = "La ubicación debe tener latitud y longitud, dentro de Río Cuarto")
    public boolean isUbicacionValida() {
        if (latitud == null && longitud == null) {
            return true;
        }
        return RioCuarto.contiene(latitud, longitud);
    }

    /**
     * Copia los datos del formulario a la propiedad. Lo usan publicar y editar; no toca quién la
     * publicó, la fuente ni el estado.
     */
    public void aplicarA(Propiedad propiedad) {
        propiedad.setTitulo(titulo);
        propiedad.setDescripcion(descripcion);
        propiedad.setPrecio(precio);
        propiedad.setExpensas(expensas);
        propiedad.setMoneda(moneda);
        propiedad.setCalle(calle);
        propiedad.setAltura(altura);
        propiedad.setPiso(piso);
        propiedad.setHabitaciones(habitaciones);
        propiedad.setMetrosCuadrados(metrosCuadrados);
        propiedad.setIncluyeImpuestos(incluyeImpuestos);
        propiedad.setPlazoActualizacionMeses(plazoActualizacionMeses);
        propiedad.setRequisitosIngreso(requisitosIngreso);
        propiedad.setLatitud(latitud);
        propiedad.setLongitud(longitud);
    }

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

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}
