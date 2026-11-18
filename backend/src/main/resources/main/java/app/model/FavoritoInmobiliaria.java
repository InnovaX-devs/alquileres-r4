package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "favoritos_inmobiliaria",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "inmobiliaria_id"})
)
public class FavoritoInmobiliaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inmobiliaria_id", nullable = false)
    private Inmobiliaria inmobiliaria;

    @Column(name = "fecha_guardado", nullable = false, updatable = false)
    private LocalDateTime fechaGuardado;

    @PrePersist
    protected void onCreate() {
        this.fechaGuardado = LocalDateTime.now();
    }

    public FavoritoInmobiliaria() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Inmobiliaria getInmobiliaria() { return inmobiliaria; }
    public void setInmobiliaria(Inmobiliaria inmobiliaria) { this.inmobiliaria = inmobiliaria; }

    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
}