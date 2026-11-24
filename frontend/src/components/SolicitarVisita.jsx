import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { listarMisVisitas } from '../services/visitas'
import { formatearFechaHora } from '../utils/visitas'
import EstadoVisita from './EstadoVisita'
import ModalSolicitarVisita from './ModalSolicitarVisita'

const claseBoton =
  'block w-full rounded-md bg-blue-600 py-2 text-center text-sm font-medium text-white hover:bg-blue-700'

/**
 * Bloque "Solicitar visita" del detalle de propiedad (#79).
 * - Sin sesión: lleva al login y vuelve a la propiedad.
 * - Con sesión: abre el modal. Si ya había pedido una visita a esta propiedad, muestra su estado.
 */
export default function SolicitarVisita({ propiedadId, tituloPropiedad }) {
  const { estaAutenticado, token } = useAuth()
  const location = useLocation()
  const boton = useRef(null)

  const [abierto, setAbierto] = useState(false)
  // Solicitud existente del usuario para esta propiedad: undefined = sin consultar, null = no tiene
  const [existente, setExistente] = useState({ token: null, solicitud: undefined })
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    if (!token) return
    let cancelado = false
    listarMisVisitas()
      .then((mias) => {
        if (cancelado) return
        const solicitud = (mias ?? []).find((s) => s.propiedadId === propiedadId) ?? null
        setExistente({ token, solicitud })
      })
      .catch(() => {
        // Si falla, se deja pedir igual: el backend responde 409 si ya existe
        if (!cancelado) setExistente({ token, solicitud: null })
      })
    return () => {
      cancelado = true
    }
  }, [token, propiedadId, intento])

  const cerrar = useCallback(() => {
    setAbierto(false)
    boton.current?.focus()
  }, [])

  // Al crearse (o si el backend dice que ya existía) se vuelve a consultar para mostrar el estado
  const alSolicitar = useCallback(() => setIntento((n) => n + 1), [])

  if (!estaAutenticado) {
    return (
      <Link to="/login" state={{ from: location }} className={claseBoton}>
        Iniciá sesión para solicitar una visita
      </Link>
    )
  }

  const solicitud = existente.token === token ? existente.solicitud : undefined

  return (
    <>
      {solicitud ? (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3 text-sm">
          <div className="flex items-center justify-between gap-2">
            <span className="font-medium text-gray-900">Ya solicitaste una visita</span>
            <EstadoVisita estado={solicitud.estado} />
          </div>
          <p className="mt-1 text-gray-600">Para el {formatearFechaHora(solicitud.fechaPropuesta)}</p>
          <Link to="/visitas" className="mt-2 inline-block text-blue-600 hover:underline">
            Ver mis solicitudes
          </Link>
        </div>
      ) : (
        <button
          ref={boton}
          type="button"
          onClick={() => setAbierto(true)}
          disabled={solicitud === undefined}
          className={`${claseBoton} disabled:opacity-60`}
        >
          Solicitar visita
        </button>
      )}

      {abierto && (
        <ModalSolicitarVisita
          propiedadId={propiedadId}
          tituloPropiedad={tituloPropiedad}
          onCerrar={cerrar}
          onSolicitada={alSolicitar}
        />
      )}
    </>
  )
}
