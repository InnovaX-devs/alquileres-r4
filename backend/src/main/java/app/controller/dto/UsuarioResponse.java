package app.controller.dto;

import app.model.Usuario;

public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String documento;
    private String descripcionPerfil;
    private boolean verificado;
    private String rol;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.telefono = usuario.getTelefono();
        this.documento = usuario.getDocumento();
        this.descripcionPerfil = usuario.getDescripcionPerfil();
        this.verificado = usuario.isVerificado();
        this.rol = usuario.getRol() != null ? usuario.getRol().name() : null;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getDocumento() { return documento; }
    public String getDescripcionPerfil() { return descripcionPerfil; }
    public boolean isVerificado() { return verificado; }
    public String getRol() { return rol; }
}