package app.controller.dto;

import app.model.EstadoPropiedad;
import jakarta.validation.constraints.NotNull;

/** Body de PATCH /propiedades/{id}/estado: marcar como alquilada o volver a publicarla. */
public class CambiarEstadoPropiedadRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoPropiedad estado;

    public EstadoPropiedad getEstado() { return estado; }
    public void setEstado(EstadoPropiedad estado) { this.estado = estado; }
}
