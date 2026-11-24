import { api } from './api'

// Aviso de que cambiaron las solicitudes (lo escucha el contador de la navbar)
const cambios = new EventTarget()

/** Registra un listener para cuando se responde una solicitud. Devuelve la función para desuscribirse. */
export function onVisitasCambiadas(listener) {
  cambios.addEventListener('cambio', listener)
  return () => cambios.removeEventListener('cambio', listener)
}

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
export async function cambiarEstadoVisita(id, estado) {
  const actualizada = await api.patch(`/visitas/${id}/estado`, { estado })
  cambios.dispatchEvent(new Event('cambio'))
  return actualizada
}
