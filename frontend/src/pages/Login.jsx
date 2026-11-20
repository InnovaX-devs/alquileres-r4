import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { ApiError } from '../services/api'
import CampoFormulario from '../components/CampoFormulario'

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
        <p className="mt-4 rounded-md bg-green-50 border border-green-200 px-3 py-2 text-sm text-green-700">
          Cuenta creada. Ya podés ingresar.
        </p>
      )}
      {estado.sesionExpirada && (
        <p className="mt-4 rounded-md bg-yellow-50 border border-yellow-200 px-3 py-2 text-sm text-yellow-800">
          Tu sesión expiró. Volvé a ingresar.
        </p>
      )}

      <form onSubmit={enviar} noValidate className="mt-6 space-y-4 bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <CampoFormulario
          id="email"
          label="Email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <CampoFormulario
          id="password"
          label="Contraseña"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {error && (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={enviando}
          className="w-full py-2 rounded-md bg-blue-600 text-white font-medium hover:bg-blue-700 disabled:opacity-60"
        >
          {enviando ? 'Ingresando...' : 'Ingresar'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm text-gray-600">
        ¿No tenés cuenta?{' '}
        <Link to="/registro" className="text-blue-600 hover:underline">
          Registrate
        </Link>
      </p>
    </div>
  )
}
