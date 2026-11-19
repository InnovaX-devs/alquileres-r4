import { API_URL } from '../config/env'
import { getToken, clearToken } from './session'

/**
 * Cliente HTTP centralizado (#65).
 *
 * - Agrega `Authorization: Bearer <token>` si hay sesión.
 * - El backend responde siempre { status, mensaje, data } (#51):
 *   si sale bien, devuelve `data`; si falla, lanza un ApiError con el mensaje del backend.
 * - Un 401 en un endpoint privado limpia la sesión y avisa para redirigir al login.
 *
 * Uso:
 *   import { api } from '../services/api'
 *   const usuario = await api.get('/usuarios/me')
 *   await api.post('/visitas', { propiedadId, fechaPropuesta })
 *   const { token } = await api.post('/auth/login', credenciales, { auth: false })
 *
 *   try { ... } catch (e) { if (e instanceof ApiError) mostrar(e.mensaje) }
 */

export class ApiError extends Error {
  constructor(status, mensaje, detalles = null) {
    super(mensaje)
    this.name = 'ApiError'
    this.status = status // 0 = no hubo respuesta (servidor caído, sin internet, CORS)
    this.mensaje = mensaje
    this.detalles = detalles // en validaciones (400): lista "campo: error"
  }
}

// Lo registra la app (ver SesionExpirada.jsx) para navegar al login sin recargar la página.
let onUnauthorized = null

export function setOnUnauthorized(handler) {
  onUnauthorized = handler
}

const MENSAJES_POR_DEFECTO = {
  0: 'No se pudo conectar con el servidor',
  401: 'Tu sesión expiró. Volvé a iniciar sesión',
  403: 'No tenés permisos para realizar esta acción',
  404: 'No se encontró lo que buscabas',
  500: 'Ocurrió un error inesperado. Intentá de nuevo más tarde',
}

async function leerCuerpo(response) {
  const texto = await response.text()
  if (!texto) return null
  try {
    return JSON.parse(texto)
  } catch {
    return null
  }
}

/**
 * @param {string} path      ruta relativa, ej. '/propiedades'
 * @param {object} opciones
 * @param {string} [opciones.method='GET']
 * @param {object|FormData} [opciones.body]  objeto (se envía como JSON) o FormData (subida de archivos)
 * @param {boolean} [opciones.auth=true]     false para login/registro: no manda token
 *                                           y un 401 no cierra la sesión (son credenciales inválidas)
 */
export async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { Accept: 'application/json' }
  const esFormData = body instanceof FormData

  if (body !== undefined && !esFormData) {
    headers['Content-Type'] = 'application/json'
  }

  const token = getToken()
  if (auth && token) {
    headers.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : esFormData ? body : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, MENSAJES_POR_DEFECTO[0])
  }

  const cuerpo = await leerCuerpo(response)

  if (response.ok) {
    return cuerpo?.data ?? null
  }

  if (response.status === 401 && auth) {
    clearToken()
    onUnauthorized?.()
  }

  const status = response.status
  const mensaje =
    cuerpo?.mensaje ||
    MENSAJES_POR_DEFECTO[status] ||
    (status >= 500 ? MENSAJES_POR_DEFECTO[500] : 'No se pudo completar la operación')
  const detalles = Array.isArray(cuerpo?.data) ? cuerpo.data : null

  throw new ApiError(status, mensaje, detalles)
}

export const api = {
  get: (path, opciones) => request(path, { ...opciones, method: 'GET' }),
  post: (path, body, opciones) => request(path, { ...opciones, method: 'POST', body }),
  put: (path, body, opciones) => request(path, { ...opciones, method: 'PUT', body }),
  patch: (path, body, opciones) => request(path, { ...opciones, method: 'PATCH', body }),
  delete: (path, opciones) => request(path, { ...opciones, method: 'DELETE' }),
}
