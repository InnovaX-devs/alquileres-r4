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
          <Button variante="secundario" tamanio="sm" to={`/alertas/nueva?${consulta}`}>
            Crear alerta
          </Button>
        </div>
      )}
      {contenido}
    </div>
  )
}
