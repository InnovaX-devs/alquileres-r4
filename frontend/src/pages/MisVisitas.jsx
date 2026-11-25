import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import PestanasVisitas from '../components/PestanasVisitas'
import EstadoVisita from '../components/EstadoVisita'
import { Cargando, ErrorConReintento, Vacio } from '../components/EstadoCarga'
import { listarMisVisitas } from '../services/visitas'
import { Card } from '../components/ui'
import { formatearFechaHora } from '../utils/visitas'

function TarjetaSolicitud({ solicitud }) {
  const { propiedadId, tituloPropiedad, fechaPropuesta, fechaSolicitud, estado, observaciones } = solicitud
  return (
    <Card as="li" padding="sm">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <Link to={`/propiedades/${propiedadId}`} className="font-bold text-rio-700 hover:underline">
          {tituloPropiedad || `Propiedad #${propiedadId}`}
        </Link>
        <EstadoVisita estado={estado} />
      </div>
      <dl className="mt-2 grid gap-x-6 gap-y-1 text-sm sm:grid-cols-2">
        <div>
          <dt className="inline text-gray-500">Fecha propuesta: </dt>
          <dd className="inline font-bold text-gray-900">{formatearFechaHora(fechaPropuesta)}</dd>
        </div>
        <div>
          <dt className="inline text-gray-500">Solicitada el: </dt>
          <dd className="inline text-gray-700">{formatearFechaHora(fechaSolicitud)}</dd>
        </div>
      </dl>
      {observaciones && <p className="mt-2 whitespace-pre-line text-sm text-gray-600">“{observaciones}”</p>}
    </Card>
  )
}

// Solicitudes de visita realizadas por el usuario (#80)
export default function MisVisitas() {
  const [resultado, setResultado] = useState(null)
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let cancelado = false
    listarMisVisitas()
      .then((datos) => {
        if (!cancelado) setResultado({ intento, solicitudes: datos ?? [], error: null })
      })
      .catch((e) => {
        if (!cancelado) setResultado({ intento, solicitudes: [], error: e.mensaje || 'No se pudieron cargar tus solicitudes' })
      })
    return () => {
      cancelado = true
    }
  }, [intento])

  let contenido
  if (!resultado || resultado.intento !== intento) {
    contenido = <Cargando etiqueta="Cargando tus solicitudes" />
  } else if (resultado.error) {
    contenido = <ErrorConReintento mensaje={resultado.error} onReintentar={() => setIntento((n) => n + 1)} />
  } else if (resultado.solicitudes.length === 0) {
    contenido = (
      <Vacio
        titulo="Todavía no solicitaste visitas"
        texto="Cuando encuentres una propiedad que te guste, pedí una visita desde su detalle."
        accion="Ver propiedades"
        to="/"
      />
    )
  } else {
    contenido = (
      <ul className="space-y-3">
        {resultado.solicitudes.map((s) => (
          <TarjetaSolicitud key={s.id} solicitud={s} />
        ))}
      </ul>
    )
  }

  return (
    <div>
      <PestanasVisitas />
      {contenido}
    </div>
  )
}
