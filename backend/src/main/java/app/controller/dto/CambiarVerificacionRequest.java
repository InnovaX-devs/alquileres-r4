package app.controller.dto;

import jakarta.validation.constraints.NotNull;

/** Body de PATCH /inmobiliarias/{id}/verificacion (issue #61, solo ADMIN). */
public class CambiarVerificacionRequest {

    @NotNull(message = "El campo verificada es obligatorio")
    private Boolean verificada;

    public Boolean getVerificada() { return verificada; }
    public void setVerificada(Boolean verificada) { this.verificada = verificada; }
}
