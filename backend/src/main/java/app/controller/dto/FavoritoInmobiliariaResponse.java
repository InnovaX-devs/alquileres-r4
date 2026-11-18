package app.controller.dto;

import app.model.FavoritoInmobiliaria;
import java.time.LocalDateTime;

public class FavoritoInmobiliariaResponse {

    private Long id;
    private Long inmobiliariaId;
    private String nombreInmobiliaria;
    private Long usuarioId;
    private LocalDateTime fechaGuardado;

    public static FavoritoInmobiliariaResponse from(FavoritoInmobiliaria favorito) {
        FavoritoInmobiliariaResponse dto = new FavoritoInmobiliariaResponse();
        dto.id = favorito.getId();
        dto.inmobiliariaId = favorito.getInmobiliaria().getId();
        dto.nombreInmobiliaria = favorito.getInmobiliaria().getNombre();
        dto.usuarioId = favorito.getUsuario().getId();
        dto.fechaGuardado = favorito.getFechaGuardado();
        return dto;
    }

    public Long getId() { return id; }
    public Long getInmobiliariaId() { return inmobiliariaId; }
    public String getNombreInmobiliaria() { return nombreInmobiliaria; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
}
