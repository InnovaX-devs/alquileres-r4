package app.controller.dto;

import app.model.Usuario;
import app.model.Inmobiliaria;

public class ContactoPublicadorResponse {
    private String nombre;
    private String email;
    private String telefono;
    private String tipo; // "USUARIO" o "INMOBILIARIA"

    public static ContactoPublicadorResponse fromUsuario(Usuario usuario) {
        ContactoPublicadorResponse dto = new ContactoPublicadorResponse();
        dto.nombre = usuario.getNombre();
        dto.email = usuario.getEmail();
        dto.telefono = usuario.getTelefono();
        dto.tipo = "USUARIO";
        return dto;
    }

    public static ContactoPublicadorResponse fromInmobiliaria(Inmobiliaria inmobiliaria) {
        ContactoPublicadorResponse dto = new ContactoPublicadorResponse();
        dto.nombre = inmobiliaria.getNombre();
        dto.email = inmobiliaria.getEmail();
        dto.telefono = inmobiliaria.getTelefono();
        dto.tipo = "INMOBILIARIA";
        return dto;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getTipo() { return tipo; }
}