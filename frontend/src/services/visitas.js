import { api } from './api'

/** Solicitar una visita: POST /visitas (#79). fechaPropuesta en formato 'YYYY-MM-DDTHH:mm:ss' (hora local). */
export function solicitarVisita({ propiedadId, fechaPropuesta, observaciones }) {
  return api.post('/visitas', { propiedadId, fechaPropuesta, observaciones: observaciones || null })
}

/** Solicitudes hechas por el usuario: GET /visitas/mias (#80). Vienen de la más nueva a la más vieja. */
export function listarMisVisitas() {
  return api.get('/visitas/mias')
}

/** Solicitudes recibidas sobre las propiedades del usuario: GET /visitas/recibidas (#81). */
export function listarVisitasRecibidas() {
  return api.get('/visitas/recibidas')
}

/** Aceptar o rechazar una solicitud: PATCH /visitas/{id}/estado (#81). estado: 'ACEPTADA' | 'RECHAZADA' */
export function cambiarEstadoVisita(id, estado) {
  return api.patch(`/visitas/${id}/estado`, { estado })
}
