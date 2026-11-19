package app.controller.dto;

public class MensajeResponse {

    private final String mensaje;

    public MensajeResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() { return mensaje; }
}
