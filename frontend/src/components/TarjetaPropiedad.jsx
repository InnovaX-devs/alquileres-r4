import { useState } from 'react'
import { Link } from 'react-router-dom'
import BotonFavorito from './BotonFavorito'
import { formatearHabitaciones, formatearPrecio, formatearUbicacion } from '../utils/propiedad'

function SinFoto() {
  return (
    <div className="h-full w-full flex flex-col items-center justify-center gap-1 bg-gray-100 text-gray-400">
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-10 w-10">
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1v-9.5Z" />
      </svg>
      <span className="text-xs">Sin fotos</span>
    </div>
  )
}

/**
 * Tarjeta del listado público (#72): foto, precio, ubicación y habitaciones.
 * Toda la tarjeta es un link al detalle de la propiedad; el corazón de favorito (#82) va encima.
 *
 * @param {object} props.propiedad  un elemento de GET /propiedades
 * @param {boolean} [props.conFavorito=true]  false en Mis propiedades (#78): no se guardan las propias
 */
export default function TarjetaPropiedad({ propiedad, conFavorito = true }) {
  const { id, titulo, precio, moneda, expensas, habitaciones, metrosCuadrados, imagenes } = propiedad
  const foto = imagenes?.[0]
  const [fotoRota, setFotoRota] = useState(false)

  const ubicacion = formatearUbicacion(propiedad)
  const ambientes = formatearHabitaciones(habitaciones)

  return (
    <div className="relative h-full">
      <Link
        to={`/propiedades/${id}`}
        className="group flex h-full flex-col overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
      >
        <div className="aspect-[4/3] overflow-hidden bg-gray-100">
          {foto && !fotoRota ? (
            <img
              src={foto}
              alt={titulo ? `Foto de ${titulo}` : `Foto de la propiedad en ${ubicacion}`}
              loading="lazy"
              onError={() => setFotoRota(true)}
              className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
            />
          ) : (
            <SinFoto />
          )}
        </div>

        <div className="flex flex-1 flex-col gap-1 p-4">
          <p className="text-xl font-bold text-gray-900">{formatearPrecio(precio, moneda)}</p>
          {expensas > 0 && (
            <p className="text-xs text-gray-500">+ {formatearPrecio(expensas, moneda)} de expensas</p>
          )}

          {titulo && <h2 className="mt-1 font-medium text-gray-900 line-clamp-1">{titulo}</h2>}

          <p className="flex items-start gap-1 text-sm text-gray-600">
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="mt-0.5 h-4 w-4 shrink-0">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 21s-7-6.2-7-11.5a7 7 0 1 1 14 0C19 14.8 12 21 12 21Z" />
              <circle cx="12" cy="9.5" r="2.5" />
            </svg>
            <span className="line-clamp-1">{ubicacion}</span>
          </p>

          {(ambientes || metrosCuadrados) && (
            <p className="mt-auto pt-2 text-sm text-gray-700">
              {[ambientes, metrosCuadrados ? `${metrosCuadrados} m²` : null].filter(Boolean).join(' · ')}
            </p>
          )}
        </div>
      </Link>
      {conFavorito && <BotonFavorito tipo="propiedad" id={id} className="absolute right-3 top-3" />}
    </div>
  )
}
