import { api } from './api'

/**
 * Crear una alerta de nuevas propiedades: POST /alertas (#85). Requiere sesión.
 * Todos los criterios son opcionales, pero tiene que venir al menos uno (ver utils/alertas).
 * Devuelve la alerta creada: { id, activa, creadaEn, precioMin, precioMax, moneda, habitacionesMin, calle }.
 */
export function crearAlerta(alerta) {
  return api.post('/alertas', alerta)
}

/** Alertas del usuario, de la más nueva a la más vieja: GET /alertas (#86). */
export function listarAlertas() {
  return api.get('/alertas')
}

/** Activa o pausa una alerta propia: PATCH /alertas/{id} (#86). Devuelve la alerta actualizada. */
export function cambiarEstadoAlerta(id, activa) {
  return api.patch(`/alertas/${id}`, { activa })
}

/** Elimina una alerta propia: DELETE /alertas/{id} (#86). */
export function eliminarAlerta(id) {
  return api.delete(`/alertas/${id}`)
}
