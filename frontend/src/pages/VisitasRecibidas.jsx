import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import PestanasVisitas from '../components/PestanasVisitas'
import EstadoVisita from '../components/EstadoVisita'
import { Cargando, ErrorConReintento, Vacio } from '../components/EstadoCarga'
import { cambiarEstadoVisita, listarVisitasRecibidas } from '../services/visitas'
import { formatearFechaHora } from '../utils/visitas'
import { formatearUbicacion } from '../utils/propiedad'

function TarjetaRecibida({ solicitud, onCambiarEstado }) {
  const { id, estado, fechaPropuesta, fechaSolicitud, observaciones, solicitante, propiedad } = solicitud
  const [guardando, setGuardando] = useState(null) // 'ACEPTADA' | 'RECHAZADA' mientras se guarda
  const [error, setError] = useState(null)

  const cambiar = async (nuevo) => {
    setError(null)
    setGuardando(nuevo)
    try {
      await onCambiarEstado(id, nuevo)
    } catch (e) {
      setError(e.mensaje || 'No se pudo actualizar la solicitud')
    } finally {
      setGuardando(null)
    }
  }

  return (
    <li className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <Link to={`/propiedades/${propiedad?.id}`} className="font-medium text-blue-700 hover:underline">
            {propiedad?.titulo || `Propiedad #${propiedad?.id}`}
          </Link>
          {propiedad && <p className="text-sm text-gray-500">{formatearUbicacion(propiedad)}</p>}
        </div>
        <EstadoVisita estado={estado} />
      </div>

      <div className="mt-3 grid gap-4 sm:grid-cols-2">
        <div className="text-sm">
          <p className="text-gray-500">Solicitante</p>
          <p className="font-medium text-gray-900">{solicitante?.nombre || 'Sin nombre'}</p>
          {solicitante?.email && (
            <a href={`mailto:${solicitante.email}`} className="block text-blue-600 hover:underline break-all">
              {solicitante.email}
            </a>
          )}
          {solicitante?.telefono && (
            <a href={`tel:${solicitante.telefono.replace(/[^\d+]/g, '')}`} className="block text-blue-600 hover:underline">
              {solicitante.telefono}
            </a>
          )}
        </div>
        <div className="text-sm">
          <p className="text-gray-500">Fecha propuesta</p>
          <p className="font-medium text-gray-900">{formatearFechaHora(fechaPropuesta)}</p>
          <p className="mt-1 text-xs text-gray-500">Solicitada el {formatearFechaHora(fechaSolicitud)}</p>
        </div>
      </div>

      {observaciones && <p className="mt-3 whitespace-pre-line text-sm text-gray-600">“{observaciones}”</p>}

      {error && (
        <p role="alert" className="mt-3 text-sm text-red-600">
          {error}
        </p>
      )}

      {/* Solo las pendientes se pueden responder: las procesadas no vuelven a PENDIENTE */}
      {estado === 'PENDIENTE' && (
        <div className="mt-4 flex justify-end gap-2">
          <button
            onClick={() => cambiar('RECHAZADA')}
            disabled={guardando !== null}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-60"
          >
            {guardando === 'RECHAZADA' ? 'Rechazando...' : 'Rechazar'}
          </button>
          <button
            onClick={() => cambiar('ACEPTADA')}
            disabled={guardando !== null}
            className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-60"
          >
            {guardando === 'ACEPTADA' ? 'Aceptando...' : 'Aceptar'}
          </button>
        </div>
      )}
    </li>
  )
}

const FILTROS = [
  { valor: 'PENDIENTE', label: 'Pendientes' },
  { valor: 'TODAS', label: 'Todas' },
]

// Solicitudes recibidas sobre las propiedades del publicador, con Aceptar y Rechazar (#81)
export default function VisitasRecibidas() {
  const [resultado, setResultado] = useState(null)
  const [intento, setIntento] = useState(0)
  const [filtro, setFiltro] = useState('PENDIENTE')
  // Respondidas en esta visita a la página: siguen visibles en "Pendientes" con su nuevo estado
  const [recienRespondidas, setRecienRespondidas] = useState(() => new Set())

  useEffect(() => {
    let cancelado = false
    listarVisitasRecibidas()
      .then((datos) => {
        if (!cancelado) setResultado({ intento, solicitudes: datos ?? [], error: null })
      })
      .catch((e) => {
        if (!cancelado) setResultado({ intento, solicitudes: [], error: e.mensaje || 'No se pudieron cargar las solicitudes' })
      })
    return () => {
      cancelado = true
    }
  }, [intento])

  // Actualiza la solicitud en la lista sin recargar la página
  const cambiarEstado = async (id, estado) => {
    const actualizada = await cambiarEstadoVisita(id, estado)
    setResultado((anterior) => ({
      ...anterior,
      solicitudes: anterior.solicitudes.map((s) => (s.id === id ? { ...s, estado: actualizada?.estado ?? estado } : s)),
    }))
    setRecienRespondidas((anterior) => new Set(anterior).add(id))
  }

  const elegirFiltro = (valor) => {
    setFiltro(valor)
    setRecienRespondidas(new Set())
  }

  let contenido
  if (!resultado || resultado.intento !== intento) {
    contenido = <Cargando etiqueta="Cargando solicitudes recibidas" />
  } else if (resultado.error) {
    contenido = <ErrorConReintento mensaje={resultado.error} onReintentar={() => setIntento((n) => n + 1)} />
  } else if (resultado.solicitudes.length === 0) {
    contenido = (
      <Vacio
        titulo="Todavía no recibiste solicitudes"
        texto="Cuando alguien pida visitar una de tus propiedades, vas a verlo acá."
        accion="Publicar una propiedad"
        to="/publicar"
      />
    )
  } else {
    const pendientes = resultado.solicitudes.filter((s) => s.estado === 'PENDIENTE')
    const visibles =
      filtro === 'PENDIENTE'
        ? resultado.solicitudes.filter((s) => s.estado === 'PENDIENTE' || recienRespondidas.has(s.id))
        : resultado.solicitudes
    contenido = (
      <>
        <div role="group" aria-label="Filtrar solicitudes" className="mb-4 inline-flex rounded-md border border-gray-200 bg-white p-1">
          {FILTROS.map(({ valor, label }) => (
            <button
              key={valor}
              onClick={() => elegirFiltro(valor)}
              aria-pressed={filtro === valor}
              className={`rounded px-3 py-1 text-sm font-medium ${
                filtro === valor ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              {label} ({valor === 'PENDIENTE' ? pendientes.length : resultado.solicitudes.length})
            </button>
          ))}
        </div>
        {visibles.length === 0 ? (
          <Vacio titulo="No tenés solicitudes pendientes" texto="Ya respondiste todas. Podés verlas en “Todas”." />
        ) : (
          <ul className="space-y-3">
            {visibles.map((s) => (
              <TarjetaRecibida key={s.id} solicitud={s} onCambiarEstado={cambiarEstado} />
            ))}
          </ul>
        )}
      </>
    )
  }

  return (
    <div>
      <PestanasVisitas />
      {contenido}
    </div>
  )
}
