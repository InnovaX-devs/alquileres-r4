import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import FormularioPerfil from '../components/FormularioPerfil'
import { ErrorConReintento } from '../components/EstadoCarga'
import { Aviso, Button, Card, CargandoPagina } from '../components/ui'
import { datosFaltantesParaVerificar, unirConY } from '../utils/perfil'
import { useToast } from '../hooks/useToast'

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
      <Aviso
        tipo="exito"
        titulo={
          <>
            <span aria-hidden="true">✓</span> Perfil verificado
          </>
        }
      >
        Tu perfil tiene todos los datos necesarios. Ya podés ver el contacto de quienes publican.
      </Aviso>
    )
  }

  const faltantes = datosFaltantesParaVerificar(usuario)

  return (
    <Aviso
      tipo="advertencia"
      titulo={
        <>
          <span aria-hidden="true">!</span> Perfil sin verificar
        </>
      }
    >
      <p>
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
        <Button variante="advertencia" tamanio="sm" onClick={onCompletar} className="mt-3">
          Completar perfil
        </Button>
      )}
    </Aviso>
  )
}

// Pantalla de perfil (#70) con formulario para completarlo (#71)
export default function Perfil() {
  const { usuario, recargarUsuario, actualizarPerfil } = useAuth()
  const [error, setError] = useState(null)
  const [actualizando, setActualizando] = useState(true)
  const [editando, setEditando] = useState(false)
  const toast = useToast()

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

  const abrirFormulario = () => setEditando(true)

  const guardar = async (cambios) => {
    const estabaVerificado = usuario.verificado
    // Si falla lanza ApiError y el formulario muestra el mensaje
    const actualizado = await actualizarPerfil(cambios)
    setEditando(false)
    setError(null)
    toast.exito(
      !estabaVerificado && actualizado.verificado
        ? 'Perfil actualizado. ¡Tu perfil quedó verificado!'
        : 'Perfil actualizado.',
    )
  }

  // Sin datos todavía (por ejemplo, el backend no respondió al cargar la sesión)
  if (!usuario) {
    if (actualizando) return <CargandoPagina etiqueta="Cargando tu perfil" />
    return (
      <div className="mx-auto max-w-xl">
        <ErrorConReintento mensaje={error || 'No se pudieron cargar tus datos'} onReintentar={reintentar} />
      </div>
    )
  }

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em]">Mi perfil</h1>

      <div className="mt-6">
        <EstadoVerificacion usuario={usuario} editando={editando} onCompletar={abrirFormulario} />
      </div>

      {/* Si falla la actualización se siguen mostrando los datos que ya había */}
      {error && (
        <Aviso tipo="error" className="mt-4">
          No se pudieron actualizar tus datos: {error}
        </Aviso>
      )}

      <Card as="section" className="mt-6">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold">{editando ? 'Editar datos personales' : 'Datos personales'}</h2>
          {!editando && (
            <Button variante="suave" tamanio="sm" onClick={abrirFormulario}>
              Editar
            </Button>
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
                  <dt className="text-sm font-bold text-gray-500">{label}</dt>
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
      </Card>
    </div>
  )
}
