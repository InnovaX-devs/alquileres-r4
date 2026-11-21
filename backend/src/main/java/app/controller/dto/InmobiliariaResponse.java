package app.controller.dto;

import app.model.Inmobiliaria;

/** Datos de una inmobiliaria (issue #61). */
public record InmobiliariaResponse(Long id, String nombre, String email, String telefono,
                                   String logoUrl, boolean verificada) {

    public static InmobiliariaResponse from(Inmobiliaria inmobiliaria) {
        return new InmobiliariaResponse(inmobiliaria.getId(), inmobiliaria.getNombre(), inmobiliaria.getEmail(),
                inmobiliaria.getTelefono(), inmobiliaria.getLogoUrl(), inmobiliaria.isVerificada());
    }
}
