import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { api } from '../services/api'
import { Aviso, Button, Card, Input } from '../components/ui'

const PASSWORD_MIN = 8
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// El backend todavía no valida estos campos, así que el formulario no deja enviar datos inválidos
function validar({ nombre, email, password, confirmacion }) {
  const errores = {}
  if (!nombre.trim()) errores.nombre = 'Ingresá tu nombre'
  if (!email.trim()) errores.email = 'Ingresá tu email'
  else if (!EMAIL_REGEX.test(email.trim())) errores.email = 'El email no es válido'
  if (password.length < PASSWORD_MIN) {
    errores.password = `La contraseña debe tener al menos ${PASSWORD_MIN} caracteres`
  }
  if (confirmacion !== password) errores.confirmacion = 'Las contraseñas no coinciden'
  return errores
}

// Pantalla de registro (#66)
export default function Register() {
  const { estaAutenticado } = useAuth()
  const navigate = useNavigate()

  const [datos, setDatos] = useState({ nombre: '', email: '', password: '', confirmacion: '' })
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (estaAutenticado) {
    return <Navigate to="/" replace />
  }

  const cambiar = (campo) => (event) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: event.target.value }))
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined }))
  }

  const enviar = async (event) => {
    event.preventDefault()
    setErrorGeneral(null)

    const encontrados = validar(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    setEnviando(true)
    const email = datos.email.trim()
    try {
      await api.post(
        '/auth/register',
        { nombre: datos.nombre.trim(), email, password: datos.password },
        { auth: false },
      )
      navigate('/login', { replace: true, state: { registrado: true, email } })
    } catch (e) {
      if (e.status === 409) {
        setErrores({ email: e.mensaje || 'El email ya está registrado' })
      } else {
        setErrorGeneral(e.detalles?.join('. ') || e.mensaje || 'No se pudo crear la cuenta')
      }
      setEnviando(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto">
      <h1 className="text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em] text-center">Crear cuenta</h1>

      <Card as="form" onSubmit={enviar} noValidate className="mt-6 space-y-4">
        <Input
          id="nombre"
          label="Nombre"
          autoComplete="name"
          value={datos.nombre}
          onChange={cambiar('nombre')}
          error={errores.nombre}
        />
        <Input
          id="email"
          label="Email"
          type="email"
          autoComplete="email"
          value={datos.email}
          onChange={cambiar('email')}
          error={errores.email}
        />
        <Input
          id="password"
          label="Contraseña"
          type="password"
          autoComplete="new-password"
          value={datos.password}
          onChange={cambiar('password')}
          error={errores.password}
        />
        <Input
          id="confirmacion"
          label="Repetir contraseña"
          type="password"
          autoComplete="new-password"
          value={datos.confirmacion}
          onChange={cambiar('confirmacion')}
          error={errores.confirmacion}
        />

        {errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}

        <Button type="submit" cargando={enviando} anchoCompleto>
          {enviando ? 'Creando cuenta...' : 'Crear cuenta'}
        </Button>
      </Card>

      <p className="mt-4 text-center text-sm text-gray-600">
        ¿Ya tenés cuenta?{' '}
        <Link to="/login" className="text-rio-600 hover:underline">
          Ingresá
        </Link>
      </p>
    </div>
  )
}
