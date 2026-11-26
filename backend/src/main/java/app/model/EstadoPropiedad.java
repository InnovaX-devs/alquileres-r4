package app.model;

/**
 * Estado de una publicación. Las ALQUILADAS dejan de aparecer en el listado público y no reciben
 * nuevos pedidos de visita, pero se siguen viendo en el detalle (por ejemplo, desde favoritos) y
 * quien la publicó la puede volver a poner como DISPONIBLE.
 */
public enum EstadoPropiedad {
    DISPONIBLE,
    ALQUILADA
}
