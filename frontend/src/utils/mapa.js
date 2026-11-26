// Constantes y helpers de los mapas (#141). Mapas con Leaflet y datos de OpenStreetMap.
import { MAPA_KEY } from '../config/env'

/** Centro de Río Cuarto: donde arranca el mapa cuando no hay ubicación. */
export const CENTRO_RIO_CUARTO = [-33.1232, -64.3493]
export const ZOOM_CIUDAD = 13
export const ZOOM_CALLE = 17

// La atribución de OpenStreetMap es obligatoria (licencia ODbL)
const ATRIBUCION_OSM = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'

/** Estilo estándar de OpenStreetMap: no necesita clave. */
const CAPA_OSM = {
  url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
  atribucion: ATRIBUCION_OSM,
  subdominios: 'abc',
  zoomMaximo: 19,
}

/** CARTO Voyager: colores más suaves y calles más claras. Necesita clave (la atribución a CARTO es obligatoria). */
function capaVoyager(clave) {
  return {
    // {r} pide las baldosas en alta resolución en pantallas retina
    url: `https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png?key=${encodeURIComponent(clave)}`,
    atribucion: `${ATRIBUCION_OSM} &copy; <a href="https://carto.com/attributions">CARTO</a>`,
    subdominios: 'abcd',
    zoomMaximo: 20,
  }
}

/** Mapa base de todos los mapas: Voyager si hay VITE_MAPA_KEY, si no OpenStreetMap. */
export const CAPA_MAPA = MAPA_KEY ? capaVoyager(MAPA_KEY) : CAPA_OSM

/** true si la propiedad tiene coordenadas para mostrar en el mapa. */
export function tieneUbicacion(propiedad) {
  return Number.isFinite(propiedad?.latitud) && Number.isFinite(propiedad?.longitud)
}

const formatoCorto = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 1 })

/** Precio corto para el pin del mapa: 350000 ARS → "$ 350 mil", 1200000 → "$ 1,2 M", 800 USD → "US$ 800". */
export function precioCorto(precio, moneda) {
  const simbolo = moneda === 'USD' ? 'US$' : '$'
  const valor = Number(precio)
  if (!Number.isFinite(valor)) return simbolo
  if (valor >= 1_000_000) return `${simbolo} ${formatoCorto.format(valor / 1_000_000)} M`
  if (valor >= 10_000) return `${simbolo} ${formatoCorto.format(Math.round(valor / 1000))} mil`
  return `${simbolo} ${formatoCorto.format(valor)}`
}
