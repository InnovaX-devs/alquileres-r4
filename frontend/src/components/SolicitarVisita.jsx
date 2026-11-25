import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { listarMisVisitas } from '../services/visitas'
import { formatearFechaHora } from '../utils/visitas'
import EstadoVisita from './EstadoVisita'
import ModalSolicitarVisita from './ModalSolicitarVisita'
import { Button } from './ui'

/**
 * Bloque "Solicitar visita" del detalle de propiedad (#79).
 * - Sin sesión: lleva al login y vuelve a la propiedad.
 * - Con sesión: abre el modal. Si ya había pedido una visita a esta propiedad, muestra su estado.
 */
export default function SolicitarVisita({ propiedadId, tituloPropiedad }) {
  const { estaAutenticado, token } = useAuth()
  const location = useLocation()

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

  // El modal devuelve el foco al botón al cerrarse
  const cerrar = useCallback(() => setAbierto(false), [])

  // Al crearse (o si el backend dice que ya existía) se vuelve a consultar para mostrar el estado
  const alSolicitar = useCallback(() => setIntento((n) => n + 1), [])

  if (!estaAutenticado) {
    return (
      <Button to="/login" state={{ from: location }} anchoCompleto tamanio="lg">
        Iniciá sesión para solicitar una visita
      </Button>
    )
  }

  const solicitud = existente.token === token ? existente.solicitud : undefined

  return (
    <>
      {solicitud ? (
        <div className="rounded-xl bg-gray-100 p-4 text-[15px]">
          <div className="flex items-center justify-between gap-2">
            <span className="font-bold text-gray-900">Ya solicitaste una visita</span>
            <EstadoVisita estado={solicitud.estado} />
          </div>
          <p className="mt-1 text-gray-600">Para el {formatearFechaHora(solicitud.fechaPropuesta)}</p>
          <Link to="/visitas" className="mt-2 inline-block font-bold text-rio-700 hover:underline">
            Ver mis solicitudes
          </Link>
        </div>
      ) : (
        <Button onClick={() => setAbierto(true)} disabled={solicitud === undefined} anchoCompleto tamanio="lg">
          Solicitar visita
        </Button>
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
