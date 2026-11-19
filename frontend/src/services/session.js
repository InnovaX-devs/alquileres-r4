// Único lugar que sabe dónde se guarda el token.
// AuthContext (#68) se suscribe a los cambios para enterarse cuando el cliente HTTP
// cierra la sesión por un 401, o cuando se inicia/cierra sesión en otra pestaña.

const TOKEN_KEY = 'token'
const listeners = new Set()

function notificar() {
  const token = getToken()
  listeners.forEach((listener) => listener(token))
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
  notificar()
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  notificar()
}

/** Registra un listener que recibe el token nuevo (o null). Devuelve la función para desuscribirse. */
export function onTokenChange(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

// Otra pestaña inició o cerró sesión
window.addEventListener('storage', (event) => {
  if (event.key === TOKEN_KEY) notificar()
})
