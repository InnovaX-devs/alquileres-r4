package app.controller.dto;

import app.model.FavoritoPropiedad;
import java.time.LocalDateTime;

public class FavoritoPropiedadResponse {

    private Long id;
    private Long propiedadId;
    private String tituloPropiedad;
    private Long usuarioId;
    private LocalDateTime fechaGuardado;

    public static FavoritoPropiedadResponse from(FavoritoPropiedad favorito) {
        FavoritoPropiedadResponse dto = new FavoritoPropiedadResponse();
        dto.id = favorito.getId();
        dto.propiedadId = favorito.getPropiedad().getId();
        dto.tituloPropiedad = favorito.getPropiedad().getTitulo();
        dto.usuarioId = favorito.getUsuario().getId();
        dto.fechaGuardado = favorito.getFechaGuardado();
        return dto;
    }

    public Long getId() { return id; }
    public Long getPropiedadId() { return propiedadId; }
    public String getTituloPropiedad() { return tituloPropiedad; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
}
