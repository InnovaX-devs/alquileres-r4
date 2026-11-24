import { api } from './api'

/** Favoritos del usuario: GET /favoritos (#35). Devuelve { propiedades: [...], inmobiliarias: [...] }. */
export function listarFavoritos() {
  return api.get('/favoritos')
}

/** Guardar / quitar una propiedad de favoritos (#82). */
export function guardarPropiedadFavorita(id) {
  return api.post(`/favoritos/propiedades/${id}`)
}

export function quitarPropiedadFavorita(id) {
  return api.delete(`/favoritos/propiedades/${id}`)
}

/** Guardar / quitar una inmobiliaria de favoritos (#83). */
export function guardarInmobiliariaFavorita(id) {
  return api.post(`/favoritos/inmobiliarias/${id}`)
}

export function quitarInmobiliariaFavorita(id) {
  return api.delete(`/favoritos/inmobiliarias/${id}`)
}
