import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import TarjetaPropiedad from '../components/TarjetaPropiedad'
import { MapaPropiedades } from '../components/mapa'
import { usePantallaGrande } from '../hooks/usePantallaGrande'
import FiltrosPropiedades from '../components/FiltrosPropiedades'
import { CLAVES_FILTROS, hayFiltrosActivos } from '../utils/filtros'
import { CLAVES_ALERTA } from '../utils/alertas'
import { listarPropiedades } from '../services/propiedades'
import { useAuth } from '../hooks/useAuth'
import { ErrorConReintento, Vacio } from '../components/EstadoCarga'

const TARJETAS_DE_CARGA = 6

// Mismo tono que el fondo de la foto de la tarjeta (alterna coral, verde agua y arena)
const TONOS_CARGA = ['bg-sol-200', 'bg-rio-100', 'bg-gray-200']

// Tarjeta con la misma forma que la real, mientras llegan los datos
function TarjetaCargando({ indice }) {
  return (
    <div aria-hidden="true" className="animate-pulse">
      <div className={`aspect-[5/4] rounded-[18px] ${TONOS_CARGA[indice % TONOS_CARGA.length]}`} />
      <div className="mt-4 space-y-2.5">
        <div className="h-7 w-1/2 rounded-lg bg-gray-200" />
        <div className="h-4 w-3/4 rounded-lg bg-gray-200" />
        <div className="h-4 w-1/3 rounded-lg bg-gray-200" />
      </div>
    </div>
  )
}

const grilla = 'grid gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3'
// Con el mapa al lado, las tarjetas van en dos columnas como máximo
const grillaConMapa = 'grid grid-cols-1 gap-x-6 gap-y-10 sm:grid-cols-2'

function SelectorVista({ vista, onCambiar }) {
  const clase = (activa) =>
    `flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium ${
      activa ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-600 hover:text-gray-900'
    }`
  return (
    <div role="group" aria-label="Ver como" className="inline-flex rounded-lg bg-gray-200 p-1 lg:hidden">
      <button type="button" aria-pressed={vista === 'lista'} onClick={() => onCambiar('lista')} className={clase(vista === 'lista')}>
        Lista
      </button>
      <button type="button" aria-pressed={vista === 'mapa'} onClick={() => onCambiar('mapa')} className={clase(vista === 'mapa')}>
        Mapa
      </button>
    </div>
  )
}

// Listado público de propiedades (#72) con filtros (#73).
// Los filtros viven en la URL (?moneda=ARS&habitacionesMin=2): se mantienen al recargar,
// funcionan con el botón atrás y una búsqueda se puede compartir con un link.
export default function Home() {
  const { estaAutenticado } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  // Mapa (#141): en pantallas grandes va al lado de la lista; en celulares se alterna
  const pantallaGrande = usePantallaGrande()
  const [vista, setVista] = useState('lista')
  const [resaltadaId, setResaltadaId] = useState(null)
  const consulta = searchParams.toString()

  const filtros = useMemo(() => {
    const params = new URLSearchParams(consulta)
    return Object.fromEntries(CLAVES_FILTROS.map((clave) => [clave, params.get(clave) ?? '']))
  }, [consulta])
  const conFiltros = hayFiltrosActivos(filtros)
  // Las alertas no entienden "Disponible ahora" (#154): sin otro criterio no se ofrece crear una
  const conCriteriosDeAlerta = CLAVES_ALERTA.some((clave) => String(filtros[clave] ?? '').trim() !== '')

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
          <TarjetaCargando key={i} indice={i} />
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
    const verLista = pantallaGrande || vista === 'lista'
    const verMapa = pantallaGrande || vista === 'mapa'
    contenido = (
      <div className="lg:grid lg:grid-cols-[minmax(0,1fr)_minmax(0,0.8fr)] lg:gap-7">
        {verLista && (
          <ul className={grillaConMapa}>
            {propiedades.map((propiedad) => (
              <li key={propiedad.id}>
                <TarjetaPropiedad
                  propiedad={propiedad}
                  resaltada={propiedad.id === resaltadaId}
                  onResaltar={setResaltadaId}
                />
              </li>
            ))}
          </ul>
        )}
        {verMapa && (
          <div className="lg:sticky lg:top-4 lg:self-start">
            <MapaPropiedades
              propiedades={propiedades}
              resaltadaId={resaltadaId}
              onResaltar={setResaltadaId}
              className="h-[70vh] lg:h-[calc(100vh-2rem)]"
            />
          </div>
        )}
      </div>
    )
  }

  const cantidad = !cargando && !error ? propiedades.length : null

  return (
    <div>
      {/* Hero: panel petróleo dentro del contenedor, con el buscador superpuesto abajo */}
      <section className="relative -mt-6 overflow-hidden rounded-[28px] bg-pino-800 px-6 pb-[116px] pt-12 sm:px-14 sm:pt-[72px]">
        {/* Decorativo: círculo coral con una casita de línea */}
        <div
          aria-hidden="true"
          className="absolute right-16 top-1/2 hidden aspect-square w-[300px] max-w-[34%] -translate-y-[58%] items-center justify-center rounded-full bg-sol-400 md:flex"
        >
          <svg viewBox="0 0 100 100" fill="none" className="w-[44%]">
            <path d="M22 80V45L50 22l28 23v35" className="stroke-pino-800" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>
        <div className="relative max-w-[620px]">
          <p className="mb-[18px] text-[15px] font-bold text-sol-400">Alquileres en Río Cuarto</p>
          <h1 className="text-[clamp(40px,5.6vw,72px)] font-bold leading-[1.02] tracking-[-0.04em] text-white">
            Tu próximo lugar te está esperando
          </h1>
          <p className="mt-5 max-w-[42ch] text-[19px] leading-relaxed text-[#e9dfcc]">
            Departamentos y casas de inmobiliarias y dueños directos, con el precio y las expensas a la vista.
          </p>
        </div>
      </section>

      <div className="relative">
        <FiltrosPropiedades valores={filtros} onAplicar={aplicarFiltros} />
      </div>

      <div className="mb-7 mt-16 flex flex-wrap items-baseline justify-between gap-x-6 gap-y-3">
        <h2 className="text-[30px] font-bold tracking-[-0.03em]">
          {cantidad === null
            ? 'Propiedades en alquiler'
            : `${cantidad} ${cantidad === 1 ? 'propiedad disponible' : 'propiedades disponibles'}`}
        </h2>
        {cantidad > 0 && <SelectorVista vista={vista} onCambiar={setVista} />}
        {conCriteriosDeAlerta && (
          // Crear una alerta con la búsqueda actual (#85): los filtros y los criterios de alerta son los mismos
          <Link
            to={`/alertas/nueva?${consulta}`}
            className="rounded text-[15px] text-gray-600 underline decoration-sol-400 decoration-2 underline-offset-[5px] hover:text-gray-900 focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600"
          >
            Avisarme cuando aparezca algo así
          </Link>
        )}
      </div>
      {contenido}
    </div>
  )
}
