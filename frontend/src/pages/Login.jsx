import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { ApiError } from '../services/api'
import { Aviso, Button, Card, Input } from '../components/ui'

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
    <div className="max-w-sm mx-auto">
      <h1 className="text-2xl font-bold text-center">Ingresar</h1>

      {estado.registrado && (
        <Aviso tipo="exito" className="mt-4">
          Cuenta creada. Ya podés ingresar.
        </Aviso>
      )}
      {estado.sesionExpirada && (
        <Aviso tipo="advertencia" className="mt-4">
          Tu sesión expiró. Volvé a ingresar.
        </Aviso>
      )}

      <Card as="form" onSubmit={enviar} noValidate className="mt-6 space-y-4">
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
        ¿No tenés cuenta?{' '}
        <Link to="/registro" className="text-blue-600 hover:underline">
          Registrate
        </Link>
      </p>
    </div>
  )
}
