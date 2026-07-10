package app.controller.dto;

import java.math.BigDecimal;

import app.model.Propiedad;

public class PropiedadDetalleResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal expensas;
    private String moneda;
    private String calle;
    private String altura;
    private String piso;
    private Integer habitaciones;
    private Integer metrosCuadrados;
    private Boolean incluyeImpuestos;
    private Integer plazoActualizacionMeses;
    private String requisitosIngreso;
    private ContactoPublicadorResponse contacto; // null si no está verificado

    public static PropiedadDetalleResponse from(Propiedad propiedad, ContactoPublicadorResponse contacto) {
        PropiedadDetalleResponse dto = new PropiedadDetalleResponse();
        dto.id = propiedad.getId();
        dto.titulo = propiedad.getTitulo();
        dto.descripcion = propiedad.getDescripcion();
        dto.precio = propiedad.getPrecio();
        dto.expensas = propiedad.getExpensas();
        dto.moneda = propiedad.getMoneda().name();
        dto.calle = propiedad.getCalle();
        dto.altura = propiedad.getAltura();
        dto.piso = propiedad.getPiso();
        dto.habitaciones = propiedad.getHabitaciones();
        dto.metrosCuadrados = propiedad.getMetrosCuadrados();
        dto.incluyeImpuestos = propiedad.getIncluyeImpuestos();
        dto.plazoActualizacionMeses = propiedad.getPlazoActualizacionMeses();
        dto.requisitosIngreso = propiedad.getRequisitosIngreso();
        dto.contacto = contacto;
        return dto;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getExpensas() { return expensas; }
    public String getMoneda() { return moneda; }
    public String getCalle() { return calle; }
    public String getAltura() { return altura; }
    public String getPiso() { return piso; }
    public Integer getHabitaciones() { return habitaciones; }
    public Integer getMetrosCuadrados() { return metrosCuadrados; }
    public Boolean getIncluyeImpuestos() { return incluyeImpuestos; }
    public Integer getPlazoActualizacionMeses() { return plazoActualizacionMeses; }
    public String getRequisitosIngreso() { return requisitosIngreso; }
    public ContactoPublicadorResponse getContacto() { return contacto; }
}