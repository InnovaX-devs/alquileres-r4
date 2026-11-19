import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export default function ProtectedRoute() {
  const location = useLocation()
  // El chequeo de token vencido se completa en el issue #69
  const { estaAutenticado } = useAuth()

  if (!estaAutenticado) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}