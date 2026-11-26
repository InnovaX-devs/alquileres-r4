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

/**
 * Publicar una propiedad: POST /propiedades (#76). Requiere sesión.
 * Devuelve la propiedad creada (con su `id`). Ver armarPropiedadRequest en utils/formularioPropiedad.
 */
export function publicarPropiedad(propiedad) {
  return api.post('/propiedades', propiedad)
}

/**
 * Subir una imagen a una propiedad: POST /propiedades/{id}/imagenes (#59, #77).
 * Va de a una: el backend la agrega al final, así que el orden es el orden en que se suben.
 * Solo puede hacerlo quien la publicó.
 */
export function subirImagenPropiedad(propiedadId, archivo) {
  const formulario = new FormData()
  formulario.append('imagen', archivo)
  return api.post(`/propiedades/${encodeURIComponent(propiedadId)}/imagenes`, formulario)
}

/** Detalle de una propiedad: GET /propiedades/{id} (#74). Con sesión verificada incluye el contacto (#75). */
export function obtenerPropiedad(id) {
  return api.get(`/propiedades/${encodeURIComponent(id)}`)
}

/** Propiedades publicadas por el usuario o su inmobiliaria: GET /propiedades/mias (#78). */
export function listarMisPropiedades() {
  return api.get('/propiedades/mias')
}

/** Editar una propiedad propia: PUT /propiedades/{id}. Mismo cuerpo que publicar. */
export function editarPropiedad(id, propiedad) {
  return api.put(`/propiedades/${encodeURIComponent(id)}`, propiedad)
}

/** Marcar como alquilada o volver a publicar: PATCH /propiedades/{id}/estado. estado: 'ALQUILADA' | 'DISPONIBLE' */
export function cambiarEstadoPropiedad(id, estado) {
  return api.patch(`/propiedades/${encodeURIComponent(id)}/estado`, { estado })
}

/** Eliminar una propiedad propia, con sus fotos: DELETE /propiedades/{id}. */
export function eliminarPropiedad(id) {
  return api.delete(`/propiedades/${encodeURIComponent(id)}`)
}

/** Quitar una foto ya subida: DELETE /propiedades/{id}/imagenes/{imagenId}. */
export function eliminarImagenPropiedad(propiedadId, imagenId) {
  return api.delete(`/propiedades/${encodeURIComponent(propiedadId)}/imagenes/${encodeURIComponent(imagenId)}`)
}
