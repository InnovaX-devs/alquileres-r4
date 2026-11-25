import { useEffect } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { tokenVencido } from '../services/jwt'
import { clearToken } from '../services/session'
import { CargandoPagina } from './ui'

/**
 * Protege las rutas privadas (#69): sin token, o con el token vencido, redirige al login
 * recordando a dónde quería ir el usuario para volver ahí después de ingresar.
 */
export default function ProtectedRoute() {
  const location = useLocation()
  const { token, cargando } = useAuth()
  const vencido = !!token && tokenVencido(token)

  useEffect(() => {
    if (vencido) clearToken()
  }, [vencido])

  if (!token || vencido) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location, sesionExpirada: vencido }}
      />
    )
  }

  // Al recargar la página: hay token pero todavía no llegó el usuario
  if (cargando) {
    return <CargandoPagina />
  }

  return <Outlet />
}
