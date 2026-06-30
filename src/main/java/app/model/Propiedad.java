package app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

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

    @Enumerated(EnumType.STRING)
    private Fuente fuente;

    private String urlOriginal;

    @ManyToOne
    @JoinColumn(name = "inmobiliaria_id")
    private Inmobiliaria inmobiliaria;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

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

    public Fuente getFuente() { return fuente; }
    public void setFuente(Fuente fuente) { this.fuente = fuente; }

    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) { this.urlOriginal = urlOriginal; }

    public Inmobiliaria getInmobiliaria() { return inmobiliaria; }
    public void setInmobiliaria(Inmobiliaria inmobiliaria) { this.inmobiliaria = inmobiliaria; }

    public Usuario getUsuario(){ return usuario;}
    public void setUsuario(Usuario usuario){ this.usuario = usuario;}
}