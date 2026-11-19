import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { getToken } from '../services/session'

export default function ProtectedRoute() {
  const location = useLocation()
  // Provisorio: se reemplaza por AuthContext en los issues #68 y #69
  const hayToken = !!getToken()

  if (!hayToken) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}