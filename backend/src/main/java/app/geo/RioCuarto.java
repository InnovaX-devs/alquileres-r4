package app.geo;

/**
 * Datos geográficos de Río Cuarto (issue #140): límites para validar coordenadas y la ubicación
 * aproximada que se muestra en público.
 */
public final class RioCuarto {

    /** Localidad censal de Río Cuarto en Georef (INDEC). */
    public static final String LOCALIDAD_CENSAL = "14098230";

    /** Centro de la ciudad, para centrar el mapa cuando no hay ubicación. */
    public static final double LATITUD_CENTRO = -33.1232;
    public static final double LONGITUD_CENTRO = -64.3493;

    // Rectángulo que contiene la ciudad y sus barrios, con un margen
    static final double LATITUD_MIN = -33.22;
    static final double LATITUD_MAX = -33.04;
    static final double LONGITUD_MIN = -64.46;
    static final double LONGITUD_MAX = -64.24;

    /** Radio del círculo que se dibuja en público alrededor de la ubicación aproximada. */
    public static final int RADIO_APROXIMADO_METROS = 150;

    // Celda de unos 170 m de lado. Un grado de latitud mide ~111.320 m; uno de longitud mide eso
    // por el coseno de la latitud (en Río Cuarto, ~0,837).
    private static final double LADO_CELDA_METROS = 170.0;
    private static final double METROS_POR_GRADO = 111_320.0;
    static final double PASO_LATITUD = LADO_CELDA_METROS / METROS_POR_GRADO;
    static final double PASO_LONGITUD =
            LADO_CELDA_METROS / (METROS_POR_GRADO * Math.cos(Math.toRadians(LATITUD_CENTRO)));

    private RioCuarto() {
    }

    /** true si las coordenadas caen dentro de Río Cuarto. */
    public static boolean contiene(Double latitud, Double longitud) {
        return latitud != null && longitud != null
                && latitud >= LATITUD_MIN && latitud <= LATITUD_MAX
                && longitud >= LONGITUD_MIN && longitud <= LONGITUD_MAX;
    }

    /**
     * Ubicación aproximada para mostrar en público: el centro de la celda de ~170 m que contiene
     * la propiedad. Todas las propiedades de la misma celda dan el mismo punto, así que no se puede
     * deducir la casa; el círculo de 150 m alrededor del centro siempre contiene la propiedad
     * (la mitad de la diagonal de la celda mide ~120 m).
     */
    public static double aproximarLatitud(double latitud) {
        return centroDeCelda(latitud, PASO_LATITUD);
    }

    public static double aproximarLongitud(double longitud) {
        return centroDeCelda(longitud, PASO_LONGITUD);
    }

    private static double centroDeCelda(double valor, double paso) {
        return (Math.floor(valor / paso) + 0.5) * paso;
    }
}
