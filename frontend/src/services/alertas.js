import { api } from './api'

/**
 * Crear una alerta de nuevas propiedades: POST /alertas (#85). Requiere sesión.
 * Todos los criterios son opcionales, pero tiene que venir al menos uno (ver utils/alertas).
 * Devuelve la alerta creada: { id, activa, creadaEn, precioMin, precioMax, moneda, habitacionesMin, calle }.
 */
export function crearAlerta(alerta) {
  return api.post('/alertas', alerta)
}
