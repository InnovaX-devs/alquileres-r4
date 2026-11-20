package app.controller.dto;

import jakarta.validation.constraints.NotNull;

/** Body de PATCH /alertas/{id}: activa o desactiva la alerta (issue #60). */
public class CambiarEstadoAlertaRequest {

    @NotNull(message = "El campo activa es obligatorio")
    private Boolean activa;

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
