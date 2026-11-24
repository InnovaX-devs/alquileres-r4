import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import TarjetaPropiedad from '../components/TarjetaPropiedad'
import FiltrosPropiedades from '../components/FiltrosPropiedades'
import { CLAVES_FILTROS, hayFiltrosActivos } from '../utils/filtros'
import { listarPropiedades } from '../services/propiedades'
import { useAuth } from '../hooks/useAuth'

const TARJETAS_DE_CARGA = 6

// Tarjeta gris con la misma forma que la real, mientras llegan los datos
function TarjetaCargando() {
  return (
    <div aria-hidden="true" className="animate-pulse overflow-hidden rounded-lg border border-gray-200 bg-white">
      <div className="aspect-[4/3] bg-gray-200" />
      <div className="space-y-2 p-4">
        <div className="h-6 w-1/2 rounded bg-gray-200" />
        <div className="h-4 w-3/4 rounded bg-gray-200" />
        <div className="h-4 w-1/3 rounded bg-gray-200" />
      </div>
    </div>
  )
}

const grilla = 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3'

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
    contenido = (
      <div className="py-16 text-center">
        <p role="alert" className="text-red-600">{error}</p>
        <button
          onClick={reintentar}
          className="mt-4 px-4 py-2 rounded-md bg-blue-600 text-white font-medium hover:bg-blue-700"
        >
          Reintentar
        </button>
      </div>
    )
  } else if (propiedades.length === 0 && conFiltros) {
    contenido = (
      <div className="rounded-lg border border-dashed border-gray-300 bg-white py-16 text-center">
        <p className="text-lg font-medium text-gray-900">No hay propiedades que coincidan con tu búsqueda</p>
        <p className="mt-1 text-sm text-gray-500">Probá con otros filtros o limpialos para ver todas.</p>
        <button
          onClick={() => aplicarFiltros({})}
          className="mt-4 inline-block px-4 py-2 rounded-md bg-blue-600 text-white text-sm font-medium hover:bg-blue-700"
        >
          Ver todas las propiedades
        </button>
      </div>
    )
  } else if (propiedades.length === 0) {
    contenido = (
      <div className="rounded-lg border border-dashed border-gray-300 bg-white py-16 text-center">
        <p className="text-lg font-medium text-gray-900">Todavía no hay propiedades publicadas</p>
        <p className="mt-1 text-sm text-gray-500">Volvé a pasar en un rato o publicá la tuya.</p>
        <Link
          to={estaAutenticado ? '/publicar' : '/login'}
          className="mt-4 inline-block px-4 py-2 rounded-md bg-blue-600 text-white text-sm font-medium hover:bg-blue-700"
        >
          Publicar una propiedad
        </Link>
      </div>
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

  return (
    <div>
      <div className="mb-6 flex items-baseline justify-between gap-4">
        <h1 className="text-2xl font-bold">Propiedades en alquiler</h1>
        {!cargando && !error && propiedades.length > 0 && (
          <p className="text-sm text-gray-500">
            {propiedades.length} {propiedades.length === 1 ? 'propiedad' : 'propiedades'}
          </p>
        )}
      </div>
      <FiltrosPropiedades valores={filtros} onAplicar={aplicarFiltros} />
      {conFiltros && (
        // Crear una alerta con la búsqueda actual (#85): los filtros y los criterios de alerta son los mismos
        <div className="mb-6 flex flex-wrap items-center justify-between gap-3 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3">
          <p className="text-sm text-blue-900">¿Querés que te avisemos cuando se publique algo con estos filtros?</p>
          <Link
            to={`/alertas/nueva?${consulta}`}
            className="rounded-md bg-white px-3 py-1.5 text-sm font-medium text-blue-700 shadow-sm hover:bg-blue-100"
          >
            Crear alerta
          </Link>
        </div>
      )}
      {contenido}
    </div>
  )
}
