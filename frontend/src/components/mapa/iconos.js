import L from 'leaflet'

// Íconos HTML propios (L.divIcon): no dependen de las imágenes de marcador de Leaflet,
// que con Vite no cargan sin configuración extra, y usan los colores de la app.

const escapar = (texto) =>
  String(texto).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c])

const cacheIconos = new Map()

/**
 * Etiqueta con el precio para el mapa del listado. Resaltada: más grande y oscura.
 * Se cachean: si se creara un ícono nuevo en cada render, Leaflet reemplazaría todos los pines
 * cada vez que se resalta una tarjeta.
 */
export function iconoPrecio(texto, resaltado) {
  const clave = `${texto}|${resaltado}`
  if (!cacheIconos.has(clave)) cacheIconos.set(clave, crearIconoPrecio(texto, resaltado))
  return cacheIconos.get(clave)
}

function crearIconoPrecio(texto, resaltado) {
  return L.divIcon({
    className: '',
    html: `<span class="mapa-precio${resaltado ? ' mapa-precio--resaltado' : ''}">${escapar(texto)}</span>`,
    iconSize: null,
    iconAnchor: [0, 0],
  })
}

/** Pin para marcar la ubicación exacta al publicar. */
export const iconoPin = L.divIcon({
  className: '',
  html: '<span class="mapa-pin" aria-hidden="true"></span>',
  iconSize: [32, 42],
  iconAnchor: [16, 42],
})
