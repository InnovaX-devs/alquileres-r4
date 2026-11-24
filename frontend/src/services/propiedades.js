import { api } from './api'

/**
 * Listado público de propiedades: GET /propiedades (#72).
 *
 * `filtros` queda preparado para el issue #73. El backend acepta (todos opcionales):
 * precioMin, precioMax, moneda (USD | ARS), habitacionesMin, calle.
 * Los valores vacíos no se mandan.
 */
export function listarPropiedades(filtros = {}) {
  const params = new URLSearchParams()
  for (const [clave, valor] of Object.entries(filtros)) {
    if (valor !== undefined && valor !== null && String(valor).trim() !== '') {
      params.set(clave, String(valor).trim())
    }
  }
  const query = params.toString()
  return api.get(query ? `/propiedades?${query}` : '/propiedades')
}

/** Detalle de una propiedad: GET /propiedades/{id} (#74). Con sesión verificada incluye el contacto (#75). */
export function obtenerPropiedad(id) {
  return api.get(`/propiedades/${encodeURIComponent(id)}`)
}
