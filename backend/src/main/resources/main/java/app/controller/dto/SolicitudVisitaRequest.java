package app.controller.dto;

import java.time.LocalDateTime;

public class SolicitudVisitaRequest {

    private Long propiedadId;
    private LocalDateTime fechaPropuesta;
    private String observaciones;

    public Long getPropiedadId() { return propiedadId; }
    public void setPropiedadId(Long propiedadId) { this.propiedadId = propiedadId; }

    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(LocalDateTime fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}