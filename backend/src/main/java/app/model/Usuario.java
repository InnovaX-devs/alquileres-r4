package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean verificado = false;

    private String telefono;

    private String documento;

    @Column(columnDefinition = "TEXT")
    private String descripcionPerfil;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inmobiliaria_id")
    private Inmobiliaria inmobiliaria;

    /** Rol dentro de la inmobiliaria; null si no pertenece a ninguna (issue #61). */
    @Enumerated(EnumType.STRING)
    private RolInmobiliaria rolInmobiliaria;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getDescripcionPerfil() { return descripcionPerfil; }
    public void setDescripcionPerfil(String descripcionPerfil) { this.descripcionPerfil = descripcionPerfil; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Inmobiliaria getInmobiliaria() { return inmobiliaria; }
    public void setInmobiliaria(Inmobiliaria inmobiliaria) { this.inmobiliaria = inmobiliaria; }

    public RolInmobiliaria getRolInmobiliaria() { return rolInmobiliaria; }
    public void setRolInmobiliaria(RolInmobiliaria rolInmobiliaria) { this.rolInmobiliaria = rolInmobiliaria; }

    /** Pertenece a la inmobiliaria con ese id (como administrador o agente). */
    public boolean esMiembroDe(Long inmobiliariaId) {
        return inmobiliaria != null && inmobiliariaId != null && inmobiliariaId.equals(inmobiliaria.getId());
    }

    /** Puede editar los datos de la inmobiliaria y gestionar sus miembros. */
    public boolean esAdministradorDe(Long inmobiliariaId) {
        return esMiembroDe(inmobiliariaId) && rolInmobiliaria == RolInmobiliaria.ADMINISTRADOR;
    }
}
