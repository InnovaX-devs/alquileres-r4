package app.controller.dto;

import app.model.FavoritoInmobiliaria;
import app.model.Inmobiliaria;
import java.time.LocalDateTime;

public class InmobiliariaFavoritaResponse {

    private Long favoritoId;
    private Long inmobiliariaId;
    private String nombre;
    private String logoUrl;
    private boolean verificada;
    private LocalDateTime fechaGuardado;

    public static InmobiliariaFavoritaResponse from(FavoritoInmobiliaria favorito) {
        Inmobiliaria inmobiliaria = favorito.getInmobiliaria();
        InmobiliariaFavoritaResponse dto = new InmobiliariaFavoritaResponse();
        dto.favoritoId = favorito.getId();
        dto.inmobiliariaId = inmobiliaria.getId();
        dto.nombre = inmobiliaria.getNombre();
        dto.logoUrl = inmobiliaria.getLogoUrl();
        dto.verificada = inmobiliaria.isVerificada();
        dto.fechaGuardado = favorito.getFechaGuardado();
        return dto;
    }

    public Long getFavoritoId() { return favoritoId; }
    public Long getInmobiliariaId() { return inmobiliariaId; }
    public String getNombre() { return nombre; }
    public String getLogoUrl() { return logoUrl; }
    public boolean isVerificada() { return verificada; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
}
