import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import TarjetaPropiedad from '../components/TarjetaPropiedad'
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

// Listado público de propiedades (#72)
export default function Home() {
  const { estaAutenticado } = useAuth()
  const [propiedades, setPropiedades] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  // Cambiarlo vuelve a disparar la carga (botón "Reintentar")
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let cancelado = false
    listarPropiedades()
      .then((datos) => {
        if (!cancelado) setPropiedades(datos ?? [])
      })
      .catch((e) => {
        if (!cancelado) setError(e.mensaje || 'No se pudieron cargar las propiedades')
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })
    return () => {
      cancelado = true
    }
  }, [intento])

  const reintentar = () => {
    setError(null)
    setCargando(true)
    setIntento((n) => n + 1)
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
      {contenido}
    </div>
  )
}
