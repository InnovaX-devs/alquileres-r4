import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import TarjetaPropiedad from '../components/TarjetaPropiedad'
import FiltrosPropiedades from '../components/FiltrosPropiedades'
import { CLAVES_FILTROS, hayFiltrosActivos } from '../utils/filtros'
import { listarPropiedades } from '../services/propiedades'
import { useAuth } from '../hooks/useAuth'
import { ErrorConReintento, Vacio } from '../components/EstadoCarga'
import { Button, CLASES_CARD } from '../components/ui'

const TARJETAS_DE_CARGA = 6

// Tarjeta gris con la misma forma que la real, mientras llegan los datos
function TarjetaCargando() {
  return (
    <div aria-hidden="true" className={`animate-pulse overflow-hidden ${CLASES_CARD}`}>
      <div className="aspect-[4/3] bg-gray-200" />
      <div className="space-y-2.5 p-5">
        <div className="h-7 w-1/2 rounded-lg bg-gray-200" />
        <div className="h-4 w-3/4 rounded-lg bg-gray-200" />
        <div className="h-4 w-1/3 rounded-lg bg-gray-200" />
      </div>
    </div>
  )
}

const grilla = 'grid gap-5 sm:grid-cols-2 lg:grid-cols-3'

// Listado público de propiedades (#72) con filtros (#73).
// Los filtros viven en la URL (?moneda=ARS&habitacionesMin=2): se mantienen al recargar,
// funcionan con el botón atrás y una búsqueda se puede compartir con un link.
export default function Home() {
  const { estaAutenticado } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const consulta = searchParams.toString()

  const filtros = useMemo(() => {
    const params = new URLSearchParams(consulta)
    return Object.fromEntries(CLAVES_FILTROS.map((clave) => [clave, params.get(clave) ?? '']))
  }, [consulta])
  const conFiltros = hayFiltrosActivos(filtros)

  // Resultado de la última carga y la búsqueda a la que corresponde
  const [resultado, setResultado] = useState(null)
  // Cambiarlo vuelve a disparar la carga (botón "Reintentar")
  const [intento, setIntento] = useState(0)
  const clave = `${consulta}#${intento}`

  useEffect(() => {
    let cancelado = false
    listarPropiedades(filtros)
      .then((datos) => {
        if (!cancelado) setResultado({ clave, propiedades: datos ?? [], error: null })
      })
      .catch((e) => {
        if (!cancelado) setResultado({ clave, propiedades: [], error: e.mensaje || 'No se pudieron cargar las propiedades' })
      })
    return () => {
      cancelado = true
    }
  }, [filtros, clave])

  const cargando = !resultado || resultado.clave !== clave
  const propiedades = cargando ? [] : resultado.propiedades
  const error = cargando ? null : resultado.error

  const reintentar = () => setIntento((n) => n + 1)

  const aplicarFiltros = (nuevos) => {
    const params = new URLSearchParams()
    for (const c of CLAVES_FILTROS) {
      const valor = String(nuevos[c] ?? '').trim()
      if (valor !== '') params.set(c, valor)
    }
    setSearchParams(params)
  }

  let contenido
  if (cargando) {
    contenido = (
      <div className={grilla} role="status" aria-label="Cargando propiedades">
        {Array.from({ length: TARJETAS_DE_CARGA }, (_, i) => (
          <TarjetaCargando key={i} />
        ))}
      </div>
    )
  } else if (error) {
    contenido = <ErrorConReintento mensaje={error} onReintentar={reintentar} />
  } else if (propiedades.length === 0 && conFiltros) {
    contenido = (
      <Vacio
        titulo="No hay propiedades que coincidan con tu búsqueda"
        texto="Probá con otros filtros o limpialos para ver todas."
        accion="Ver todas las propiedades"
        onAccion={() => aplicarFiltros({})}
      />
    )
  } else if (propiedades.length === 0) {
    contenido = (
      <Vacio
        titulo="Todavía no hay propiedades publicadas"
        texto="Volvé a pasar en un rato o publicá la tuya."
        accion="Publicar una propiedad"
        to={estaAutenticado ? '/publicar' : '/login'}
      />
    )
  } else {
    contenido = (
      <ul className={grilla}>
        {propiedades.map((propiedad) => (
          <li key={propiedad.id}>
            <TarjetaPropiedad propiedad={propiedad} />
          </li>
        ))}
      </ul>
    )
  }

  const cantidad = !cargando && !error ? propiedades.length : null

  return (
    <div>
      {/* Hero de ancho completo: se sale del contenedor del Layout */}
      <section className="relative left-1/2 -mt-8 w-screen -translate-x-1/2 bg-pino-800 pb-28 pt-12 text-white sm:pt-16">
        <div className="mx-auto max-w-6xl px-4">
          <h1 className="max-w-[13ch] text-[clamp(2.4rem,6vw,4.25rem)] font-extrabold leading-[1.02] tracking-[-0.035em]">
            Tu próximo lugar en Río Cuarto
          </h1>
          <p className="mt-4 max-w-[52ch] text-lg leading-relaxed text-gray-300">
            Departamentos y casas de inmobiliarias y dueños directos, con el precio y las expensas a la vista.
          </p>
        </div>
      </section>

      <div className="relative -mt-20">
        <FiltrosPropiedades valores={filtros} onAplicar={aplicarFiltros} />
      </div>

      <div className="mb-5 mt-10 flex flex-wrap items-baseline justify-between gap-x-6 gap-y-2">
        <h2 className="text-[1.75rem] font-bold">
          {cantidad === null
            ? 'Propiedades en alquiler'
            : `${cantidad} ${cantidad === 1 ? 'propiedad disponible' : 'propiedades disponibles'}`}
        </h2>
        {conFiltros && (
          // Crear una alerta con la búsqueda actual (#85): los filtros y los criterios de alerta son los mismos
          <Button variante="suave" tamanio="sm" to={`/alertas/nueva?${consulta}`}>
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-[18px] w-[18px]">
              <path d="M6 8a6 6 0 1 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
              <path d="M10 21a2 2 0 0 0 4 0" />
            </svg>
            Avisarme cuando aparezca algo así
          </Button>
        )}
      </div>
      {contenido}
    </div>
  )
}
