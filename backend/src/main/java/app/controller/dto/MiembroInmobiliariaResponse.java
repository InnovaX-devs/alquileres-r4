package app.controller.dto;

import app.model.Usuario;

/** Miembro de una inmobiliaria (issue #61). */
public record MiembroInmobiliariaResponse(Long id, String nombre, String email, String rolInmobiliaria) {

    public static MiembroInmobiliariaResponse from(Usuario usuario) {
        return new MiembroInmobiliariaResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getRolInmobiliaria() != null ? usuario.getRolInmobiliaria().name() : null);
    }
}
