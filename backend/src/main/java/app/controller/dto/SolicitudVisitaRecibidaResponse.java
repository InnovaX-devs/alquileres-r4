package app.controller.dto;

import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Solicitud de visita vista por el publicador (issue #57): incluye datos del solicitante y de la propiedad. */
public class SolicitudVisitaRecibidaResponse {

    private Long id;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaPropuesta;
    private String observaciones;
    private SolicitanteInfo solicitante;
    private PropiedadInfo propiedad;

    public static SolicitudVisitaRecibidaResponse from(SolicitudVisita solicitud) {
        SolicitudVisitaRecibidaResponse dto = new SolicitudVisitaRecibidaResponse();
        dto.id = solicitud.getId();
        dto.estado = solicitud.getEstado().name();
        dto.fechaSolicitud = solicitud.getFechaSolicitud();
        dto.fechaPropuesta = solicitud.getFechaPropuesta();
        dto.observaciones = solicitud.getObservaciones();
        dto.solicitante = SolicitanteInfo.from(solicitud.getUsuario());
        dto.propiedad = PropiedadInfo.from(solicitud.getPropiedad());
        return dto;
    }

    public Long getId() { return id; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public String getObservaciones() { return observaciones; }
    public SolicitanteInfo getSolicitante() { return solicitante; }
    public PropiedadInfo getPropiedad() { return propiedad; }

    public static class SolicitanteInfo {
        private Long id;
        private String nombre;
        private String email;
        private String telefono;

        static SolicitanteInfo from(Usuario usuario) {
            SolicitanteInfo info = new SolicitanteInfo();
            info.id = usuario.getId();
            info.nombre = usuario.getNombre();
            info.email = usuario.getEmail();
            info.telefono = usuario.getTelefono();
            return info;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public String getTelefono() { return telefono; }
    }

    public static class PropiedadInfo {
        private Long id;
        private String titulo;
        private String calle;
        private String altura;
        private String piso;
        private BigDecimal precio;
        private String moneda;

        static PropiedadInfo from(Propiedad propiedad) {
            PropiedadInfo info = new PropiedadInfo();
            info.id = propiedad.getId();
            info.titulo = propiedad.getTitulo();
            info.calle = propiedad.getCalle();
            info.altura = propiedad.getAltura();
            info.piso = propiedad.getPiso();
            info.precio = propiedad.getPrecio();
            info.moneda = propiedad.getMoneda() != null ? propiedad.getMoneda().name() : null;
            return info;
        }

        public Long getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getCalle() { return calle; }
        public String getAltura() { return altura; }
        public String getPiso() { return piso; }
        public BigDecimal getPrecio() { return precio; }
        public String getMoneda() { return moneda; }
    }
}
