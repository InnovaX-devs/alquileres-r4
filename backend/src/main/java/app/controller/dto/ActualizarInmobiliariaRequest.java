package app.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body de PATCH /inmobiliarias/{id} (issue #61). Todos los campos son opcionales:
 * solo se modifican los que vienen informados.
 */
public class ActualizarInmobiliariaRequest {

    @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @Email(message = "El email no tiene un formato válido")
    @Pattern(regexp = ".*\\S.*", message = "El email no puede estar vacío")
    @Size(max = 255, message = "El email no puede superar los 255 caracteres")
    private String email;

    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres")
    private String telefono;

    @Size(max = 255, message = "La URL del logo no puede superar los 255 caracteres")
    private String logoUrl;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}
