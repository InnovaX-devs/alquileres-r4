import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { ApiError } from '../services/api'
import { Aviso, Button, Card, Input } from '../components/ui'
import PantallaAcceso from '../components/PantallaAcceso'

// Pantalla de login (#67)
export default function Login() {
  const { login, estaAutenticado } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const estado = location.state ?? {}

  const [email, setEmail] = useState(estado.email ?? '')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  // A dónde volver después de ingresar (lo guarda ProtectedRoute)
  const destino = estado.from?.pathname && estado.from.pathname !== '/login' ? estado.from.pathname : '/'

  if (estaAutenticado && !enviando) {
    return <Navigate to={destino} replace />
  }

  const enviar = async (event) => {
    event.preventDefault()
    setError(null)

    if (!email.trim() || !password) {
      setError('Completá el email y la contraseña')
      return
    }

    setEnviando(true)
    try {
      await login(email.trim(), password)
      navigate(destino, { replace: true })
    } catch (e) {
      setError(
        e instanceof ApiError && e.status === 401
          ? 'Email o contraseña incorrectos'
          : e.mensaje || 'No se pudo iniciar sesión',
      )
      setEnviando(false)
    }
  }

  return (
    <PantallaAcceso titulo="Ingresar">
      {estado.registrado && (
        <Aviso tipo="exito" className="mb-4">
          Cuenta creada. Ya podés ingresar.
        </Aviso>
      )}
      {estado.sesionExpirada && (
        <Aviso tipo="advertencia" className="mb-4">
          Tu sesión expiró. Volvé a ingresar.
        </Aviso>
      )}

      <Card as="form" onSubmit={enviar} noValidate className="space-y-4">
        <Input
          id="email"
          label="Email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          id="password"
          label="Contraseña"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {error && <Aviso tipo="error">{error}</Aviso>}

        <Button type="submit" cargando={enviando} anchoCompleto>
          {enviando ? 'Ingresando...' : 'Ingresar'}
        </Button>
      </Card>

      <p className="mt-4 text-center text-sm text-gray-600">
        ¿Sos nuevo?{' '}
        <Link to="/registro" className="font-bold text-rio-600 hover:text-gray-900">
          Registrate
        </Link>
      </p>
    </PantallaAcceso>
  )
}
