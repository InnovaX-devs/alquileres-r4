package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "solicitudes_visita",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "propiedad_id"})
)
public class SolicitudVisita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(nullable = false)
    private LocalDateTime fechaPropuesta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    private String observaciones;

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }

    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(LocalDateTime fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}