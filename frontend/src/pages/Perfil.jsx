import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import FormularioPerfil from '../components/FormularioPerfil'
import { datosFaltantesParaVerificar, unirConY } from '../utils/perfil'

/** Datos personales que se muestran, en orden. `clave` es el campo de GET /usuarios/me. */
const CAMPOS = [
  { clave: 'nombre', label: 'Nombre' },
  { clave: 'email', label: 'Email' },
  { clave: 'telefono', label: 'Teléfono' },
  { clave: 'documento', label: 'Documento' },
  { clave: 'descripcionPerfil', label: 'Descripción' },
]

// El backend marca el perfil como verificado solo cuando tiene nombre, teléfono y documento (#71)
function EstadoVerificacion({ usuario, editando, onCompletar }) {
  if (usuario.verificado) {
    return (
      <div className="rounded-md bg-green-50 border border-green-200 px-4 py-3">
        <p className="flex items-center gap-2 font-medium text-green-800">
          <span aria-hidden="true">✓</span> Perfil verificado
        </p>
        <p className="mt-1 text-sm text-green-700">
          Tu perfil tiene todos los datos necesarios. Ya podés ver el contacto de quienes publican.
        </p>
      </div>
    )
  }

  const faltantes = datosFaltantesParaVerificar(usuario)

  return (
    <div className="rounded-md bg-yellow-50 border border-yellow-200 px-4 py-3">
      <p className="flex items-center gap-2 font-medium text-yellow-800">
        <span aria-hidden="true">!</span> Perfil sin verificar
      </p>
      <p className="mt-1 text-sm text-yellow-700">
        {faltantes.length > 0 ? (
          <>
            Para verificar tu perfil te falta completar: <strong>{unirConY(faltantes)}</strong>.
          </>
        ) : (
          // Tiene todo pero el flag quedó en false (el backend lo recalcula al guardar el perfil)
          'Tus datos están completos. Guardá tu perfil para que se verifique.'
        )}{' '}
        Mientras tanto no vas a poder ver el contacto de quienes publican.
      </p>
      {!editando && (
        <button
          onClick={onCompletar}
          className="mt-3 px-3 py-1.5 rounded-md bg-yellow-600 text-white text-sm font-medium hover:bg-yellow-700"
        >
          Completar perfil
        </button>
      )}
    </div>
  )
}

// Pantalla de perfil (#70) con formulario para completarlo (#71)
export default function Perfil() {
  const { usuario, recargarUsuario, actualizarPerfil } = useAuth()
  const [error, setError] = useState(null)
  const [actualizando, setActualizando] = useState(true)
  const [editando, setEditando] = useState(false)
  const [exito, setExito] = useState(null)

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

  // El usuario ya viene del contexto, pero se vuelve a pedir al entrar para mostrar los datos
  // al día (pudieron cambiarse desde otra pestaña o dispositivo)
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

  const abrirFormulario = () => {
    setExito(null)
    setEditando(true)
  }

  const guardar = async (cambios) => {
    const estabaVerificado = usuario.verificado
    // Si falla lanza ApiError y el formulario muestra el mensaje
    const actualizado = await actualizarPerfil(cambios)
    setEditando(false)
    setError(null)
    setExito(
      !estabaVerificado && actualizado.verificado
        ? 'Perfil actualizado. ¡Tu perfil quedó verificado!'
        : 'Perfil actualizado.',
    )
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
        <EstadoVerificacion usuario={usuario} editando={editando} onCompletar={abrirFormulario} />
      </div>

      {exito && (
        <p role="status" className="mt-4 rounded-md bg-green-50 border border-green-200 px-3 py-2 text-sm text-green-700">
          {exito}
        </p>
      )}

      {/* Si falla la actualización se siguen mostrando los datos que ya había */}
      {error && (
        <p role="alert" className="mt-4 text-sm text-red-600">
          No se pudieron actualizar tus datos: {error}
        </p>
      )}

      <section className="mt-6 bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">{editando ? 'Editar datos personales' : 'Datos personales'}</h2>
          {!editando && (
            <button
              onClick={abrirFormulario}
              className="px-3 py-1.5 rounded-md text-sm font-medium text-blue-600 hover:bg-blue-50"
            >
              Editar
            </button>
          )}
        </div>

        {editando ? (
          <FormularioPerfil
            usuario={usuario}
            onGuardar={guardar}
            onCancelar={() => setEditando(false)}
          />
        ) : (
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
        )}
      </section>
    </div>
  )
}
