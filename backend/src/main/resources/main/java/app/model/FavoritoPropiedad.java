package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "favoritos_propiedad",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "propiedad_id"})
)
public class FavoritoPropiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @Column(name = "fecha_guardado", nullable = false, updatable = false)
    private LocalDateTime fechaGuardado;

    @PrePersist
    protected void onCreate() {
        this.fechaGuardado = LocalDateTime.now();
    }

    public FavoritoPropiedad() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }

    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
}
