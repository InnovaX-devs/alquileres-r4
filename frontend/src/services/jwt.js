// Lectura del vencimiento del JWT del lado del cliente (#69).
// No valida la firma (eso lo hace el backend): solo evita usar un token que ya sabemos vencido.

/** Margen para no mandar un token que vence durante el request. */
const MARGEN_MS = 30_000

function leerPayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64))
  } catch {
    return null
  }
}

/** Fecha de vencimiento en milisegundos, o null si el token no se puede leer. */
export function vencimientoMs(token) {
  const exp = leerPayload(token)?.exp
  return typeof exp === 'number' ? exp * 1000 : null
}

/** true si no hay token, no se puede leer, o ya venció (o vence en menos de 30 s). */
export function tokenVencido(token, ahora = Date.now()) {
  if (!token) return true
  const vence = vencimientoMs(token)
  return vence === null || vence - MARGEN_MS <= ahora
}
