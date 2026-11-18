package app.controller.dto;

import java.util.List;

public class FavoritosResponse {

    private final List<PropiedadFavoritaResponse> propiedades;
    private final List<InmobiliariaFavoritaResponse> inmobiliarias;

    public FavoritosResponse(List<PropiedadFavoritaResponse> propiedades,
                             List<InmobiliariaFavoritaResponse> inmobiliarias) {
        this.propiedades = propiedades;
        this.inmobiliarias = inmobiliarias;
    }

    public List<PropiedadFavoritaResponse> getPropiedades() { return propiedades; }
    public List<InmobiliariaFavoritaResponse> getInmobiliarias() { return inmobiliarias; }
}
