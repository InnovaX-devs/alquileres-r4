import { useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { setOnUnauthorized } from '../services/api'

// Cuando el cliente HTTP recibe un 401 (token vencido o inválido), ya limpió la sesión:
// acá solo se redirige al login, recordando a dónde quería ir el usuario.
export default function SesionExpirada() {
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    setOnUnauthorized(() => {
      navigate('/login', { replace: true, state: { from: location, sesionExpirada: true } })
    })
    return () => setOnUnauthorized(null)
  }, [navigate, location])

  return null
}
