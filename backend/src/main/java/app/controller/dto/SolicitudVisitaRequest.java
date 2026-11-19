package app.controller.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class SolicitudVisitaRequest {

    @NotNull(message = "La propiedad es obligatoria")
    private Long propiedadId;
    @NotNull(message = "La fecha propuesta es obligatoria")
    @Future(message = "La fecha propuesta debe ser futura")
    private LocalDateTime fechaPropuesta;
    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    private String observaciones;

    public Long getPropiedadId() { return propiedadId; }
    public void setPropiedadId(Long propiedadId) { this.propiedadId = propiedadId; }

    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(LocalDateTime fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}