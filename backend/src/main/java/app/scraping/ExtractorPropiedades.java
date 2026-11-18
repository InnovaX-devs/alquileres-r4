package app.scraping;

import app.model.FuenteExterna;

import java.util.List;

/**
 * Lee propiedades desde un tipo de fuente externa. Para soportar un portal
 * nuevo alcanza con agregar un @Component que implemente esta interfaz.
 */
public interface ExtractorPropiedades {

    boolean soporta(FuenteExterna fuente);

    List<PropiedadExterna> extraer(FuenteExterna fuente);
}
