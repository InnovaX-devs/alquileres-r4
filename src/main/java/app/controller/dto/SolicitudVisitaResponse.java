package app.controller.dto;

import app.model.SolicitudVisita;
import java.time.LocalDateTime;

public class SolicitudVisitaResponse {

    private Long id;
    private Long propiedadId;
    private String titulopropiedad;
    private Long usuarioId;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaPropuesta;
    private String estado;
    private String observaciones;

    public static SolicitudVisitaResponse from(SolicitudVisita solicitud) {
        SolicitudVisitaResponse dto = new SolicitudVisitaResponse();
        dto.id = solicitud.getId();
        dto.propiedadId = solicitud.getPropiedad().getId();
        dto.titulopropiedad = solicitud.getPropiedad().getTitulo();
        dto.usuarioId = solicitud.getUsuario().getId();
        dto.fechaSolicitud = solicitud.getFechaSolicitud();
        dto.fechaPropuesta = solicitud.getFechaPropuesta();
        dto.estado = solicitud.getEstado().name();
        dto.observaciones = solicitud.getObservaciones();
        return dto;
    }

    public Long getId() { return id; }
    public Long getPropiedadId() { return propiedadId; }
    public String getTituloPropiedad() { return titulopropiedad; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
}