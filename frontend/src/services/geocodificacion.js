import { api } from './api'

/**
 * Sugerencia de ubicación para una dirección de Río Cuarto: GET /geocodificacion (#140).
 * Devuelve { latitud, longitud, direccion } o null si el backend no encontró una confiable.
 */
export function sugerirUbicacion(calle, altura) {
  const params = new URLSearchParams({ calle })
  if (altura) params.set('altura', altura)
  return api.get(`/geocodificacion?${params}`)
}
