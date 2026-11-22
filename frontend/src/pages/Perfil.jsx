import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../hooks/useAuth'

/** Datos personales que se muestran, en orden. `clave` es el campo de GET /usuarios/me. */
const CAMPOS = [
  { clave: 'nombre', label: 'Nombre' },
  { clave: 'email', label: 'Email' },
  { clave: 'telefono', label: 'Teléfono' },
  { clave: 'documento', label: 'Documento' },
  { clave: 'descripcionPerfil', label: 'Descripción' },
]

function EstadoVerificacion({ verificado }) {
  if (verificado) {
    return (
      <div className="rounded-md bg-green-50 border border-green-200 px-4 py-3">
        <p className="flex items-center gap-2 font-medium text-green-800">
          <span aria-hidden="true">✓</span> Perfil verificado
        </p>
        <p className="mt-1 text-sm text-green-700">Tu identidad fue verificada por la plataforma.</p>
      </div>
    )
  }

  return (
    <div className="rounded-md bg-yellow-50 border border-yellow-200 px-4 py-3">
      <p className="flex items-center gap-2 font-medium text-yellow-800">
        <span aria-hidden="true">!</span> Perfil sin verificar
      </p>
      <p className="mt-1 text-sm text-yellow-700">
        Tu perfil todavía no fue verificado por la plataforma.
      </p>
    </div>
  )
}

// Pantalla de perfil (#70)
export default function Perfil() {
  const { usuario, recargarUsuario } = useAuth()
  const [error, setError] = useState(null)
  const [actualizando, setActualizando] = useState(true)

  // Pide de nuevo GET /usuarios/me (lo guarda el AuthContext, que es de donde se lee `usuario`)
  const pedirUsuario = useCallback(
    (estaCancelado = () => false) =>
      recargarUsuario()
        .catch((e) => {
          // 401: el cliente HTTP ya cerró la sesión y ProtectedRoute redirige al login
          if (!estaCancelado()) setError(e.mensaje || 'No se pudieron cargar tus datos')
        })
        .finally(() => {
          if (!estaCancelado()) setActualizando(false)
        }),
    [recargarUsuario],
  )

  // El usuario ya viene del contexto, pero se vuelve a pedir al entrar para que el
  // estado de verificación esté al día (un admin pudo haberlo verificado mientras tanto)
  useEffect(() => {
    let cancelado = false
    pedirUsuario(() => cancelado)
    return () => {
      cancelado = true
    }
  }, [pedirUsuario])

  const reintentar = () => {
    setError(null)
    setActualizando(true)
    pedirUsuario()
  }

  // Sin datos todavía (por ejemplo, el backend no respondió al cargar la sesión)
  if (!usuario) {
    if (actualizando) {
      return <p className="text-center text-gray-500 py-16">Cargando...</p>
    }
    return (
      <div className="max-w-xl mx-auto text-center py-16">
        <p role="alert" className="text-red-600">{error || 'No se pudieron cargar tus datos'}</p>
        <button
          onClick={reintentar}
          className="mt-4 px-4 py-2 rounded-md bg-blue-600 text-white font-medium hover:bg-blue-700"
        >
          Reintentar
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-2xl font-bold">Mi perfil</h1>

      <div className="mt-6">
        <EstadoVerificacion verificado={usuario.verificado} />
      </div>

      {/* Si falla la actualización se siguen mostrando los datos que ya había */}
      {error && (
        <p role="alert" className="mt-4 text-sm text-red-600">
          No se pudieron actualizar tus datos: {error}
        </p>
      )}

      <section className="mt-6 bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h2 className="text-lg font-semibold">Datos personales</h2>
        <dl className="mt-4 divide-y divide-gray-100">
          {CAMPOS.map(({ clave, label }) => {
            const valor = usuario[clave]?.toString().trim()
            return (
              <div key={clave} className="py-3 sm:grid sm:grid-cols-3 sm:gap-4">
                <dt className="text-sm font-medium text-gray-500">{label}</dt>
                <dd
                  className={`mt-1 sm:mt-0 sm:col-span-2 text-sm break-words whitespace-pre-line ${
                    valor ? 'text-gray-900' : 'text-gray-400 italic'
                  }`}
                >
                  {valor || 'No informado'}
                </dd>
              </div>
            )
          })}
        </dl>
      </section>
    </div>
  )
}
