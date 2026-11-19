import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { api } from '../services/api'
import { AuthContext } from './authContextValue'
import { getToken, setToken, clearToken, onTokenChange } from '../services/session'

/**
 * Sesión global (#68): token + usuario autenticado.
 *
 * - El token se guarda en localStorage (services/session.js), así la sesión sobrevive a un F5.
 * - Con token, el usuario se carga desde GET /usuarios/me.
 * - Si el token vence, el cliente HTTP recibe un 401, lo borra y este contexto se entera solo.
 *
 * Se consume con el hook useAuth() (hooks/useAuth.js).
 */
export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(getToken)
  const [usuario, setUsuario] = useState(null)
  // Hay token pero todavía no llegó el usuario (al recargar la página)
  const [cargando, setCargando] = useState(() => !!getToken())
  // Token para el que ya se cargó el usuario (evita pedir /usuarios/me dos veces tras el login)
  const usuarioCargadoPara = useRef(null)

  // Mantener el estado sincronizado con session.js (401, logout, otra pestaña)
  useEffect(
    () =>
      onTokenChange((nuevo) => {
        setTokenState(nuevo)
        if (!nuevo) {
          usuarioCargadoPara.current = null
          setUsuario(null)
          setCargando(false)
        }
      }),
    [],
  )

  // Cargar el usuario cada vez que cambia el token
  useEffect(() => {
    if (!token || usuarioCargadoPara.current === token) return

    let cancelado = false
    setCargando(true)
    api
      .get('/usuarios/me')
      .then((datos) => {
        if (!cancelado) {
          usuarioCargadoPara.current = token
          setUsuario(datos)
        }
      })
      .catch(() => {
        // 401: el cliente HTTP ya limpió la sesión. Otro error (servidor caído):
        // se mantiene el token y el usuario queda en null hasta el próximo intento.
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })

    return () => {
      cancelado = true
    }
  }, [token])

  /**
   * Inicia sesión. Si las credenciales son inválidas lanza un ApiError (401) con el mensaje
   * del backend. La sesión se guarda recién cuando se pudo cargar el usuario.
   */
  const login = useCallback(async (email, password) => {
    const { token: nuevoToken } = await api.post('/auth/login', { email, password }, { auth: false })
    const datos = await api.get('/usuarios/me', { token: nuevoToken })
    usuarioCargadoPara.current = nuevoToken
    setUsuario(datos)
    setCargando(false)
    setToken(nuevoToken)
    return datos
  }, [])

  const logout = useCallback(() => {
    clearToken()
  }, [])

  /** Vuelve a pedir /usuarios/me (por ejemplo, después de completar el perfil). */
  const recargarUsuario = useCallback(async () => {
    const datos = await api.get('/usuarios/me')
    usuarioCargadoPara.current = getToken()
    setUsuario(datos)
    return datos
  }, [])

  const valor = useMemo(
    () => ({
      token,
      usuario,
      cargando,
      estaAutenticado: !!token,
      login,
      logout,
      recargarUsuario,
    }),
    [token, usuario, cargando, login, logout, recargarUsuario],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
