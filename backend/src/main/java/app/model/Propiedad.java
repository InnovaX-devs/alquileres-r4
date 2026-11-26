package app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Entity
@Table(name = "propiedad")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private BigDecimal precio;

    private BigDecimal expensas;

    @Enumerated(EnumType.STRING)
    private Moneda moneda;

    private String calle;

    private String altura;

    private String piso;

    private Integer habitaciones;

    private Integer metrosCuadrados;

    private Boolean incluyeImpuestos;

    private Integer plazoActualizacionMeses;

    @Column(columnDefinition = "TEXT")
    private String requisitosIngreso;

    // Desde cuándo se puede entrar (issue #154). Null = disponible ya.
    private LocalDate disponibleDesde;

    @Enumerated(EnumType.STRING)
    private Fuente fuente;

    private String urlOriginal;

    // Ubicación exacta que confirmó quien publica (issue #140). Opcionales, pero van las dos juntas.
    // En público se muestra aproximada (ver RioCuarto.aproximarLatitud).
    private Double latitud;

    private Double longitud;

    // Las propiedades publicadas antes de que existiera este campo quedan con null: cuentan como
    // DISPONIBLE (ver getEstado).
    @Enumerated(EnumType.STRING)
    private EstadoPropiedad estado = EstadoPropiedad.DISPONIBLE;

    @ManyToOne
    @JoinColumn(name = "inmobiliaria_id")
    private Inmobiliaria inmobiliaria;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL)
    @OrderBy("orden ASC, id ASC")
    private List<ImagenPropiedad> imagenes;

    /**
     * Quién puede gestionar la propiedad (subir imágenes, responder solicitudes de visita, etc.):
     * - Si se publicó a nombre de una inmobiliaria, es de la inmobiliaria: la gestionan sus miembros
     *   actuales. Quien la cargó deja de poder gestionarla si lo desvinculan de la inmobiliaria.
     * - Si se publicó como particular (sin inmobiliaria), la gestiona solo quien la creó.
     */
    public boolean esPublicadaPor(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) return false;

        if (this.inmobiliaria != null) {
            return usuario.esMiembroDe(this.inmobiliaria.getId());
        }

        return this.usuario != null && usuario.getId().equals(this.usuario.getId());
    }

    /** Zona horaria de los usuarios (Río Cuarto): el servidor corre en UTC y de noche ya estaría en el día siguiente. */
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Argentina/Cordoba");

    /** La fecha de hoy para quienes usan la app. */
    public static LocalDate hoy() {
        return LocalDate.now(ZONA_HORARIA);
    }

    public boolean estaAlquilada() {
        return getEstado() == EstadoPropiedad.ALQUILADA;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDate getDisponibleDesde() { return disponibleDesde; }
    public void setDisponibleDesde(LocalDate disponibleDesde) { this.disponibleDesde = disponibleDesde; }

    public Fuente getFuente() { return fuente; }
    public void setFuente(Fuente fuente) { this.fuente = fuente; }

    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) { this.urlOriginal = urlOriginal; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public EstadoPropiedad getEstado() { return estado != null ? estado : EstadoPropiedad.DISPONIBLE; }
    public void setEstado(EstadoPropiedad estado) { this.estado = estado; }

    public Inmobiliaria getInmobiliaria() { return inmobiliaria; }
    public void setInmobiliaria(Inmobiliaria inmobiliaria) { this.inmobiliaria = inmobiliaria; }

    public Usuario getUsuario(){ return usuario;}
    public void setUsuario(Usuario usuario){ this.usuario = usuario;}

    public List<ImagenPropiedad> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenPropiedad> imagenes) { this.imagenes = imagenes; }
}