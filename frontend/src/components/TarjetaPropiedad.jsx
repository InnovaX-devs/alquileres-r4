import { useState } from 'react'
import { Link } from 'react-router-dom'
import BotonFavorito from './BotonFavorito'
import { Card } from './ui'
import { formatearHabitaciones, formatearPrecio, formatearUbicacion } from '../utils/propiedad'

function SinFoto() {
  return (
    <div className="flex h-full w-full flex-col items-center justify-center gap-1 bg-gray-200/70 text-gray-500">
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
      <Card
        as={Link}
        to={`/propiedades/${id}`}
        padding="ninguno"
        className="group flex h-full flex-col overflow-hidden transition duration-200 hover:-translate-y-0.5 hover:border-gray-300 hover:shadow-xl hover:shadow-pino-900/10 focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600"
      >
        <div className="aspect-[4/3] overflow-hidden bg-gray-100">
          {foto && !fotoRota ? (
            <img
              src={foto}
              alt={titulo ? `Foto de ${titulo}` : `Foto de la propiedad en ${ubicacion}`}
              loading="lazy"
              onError={() => setFotoRota(true)}
              className="h-full w-full object-cover transition duration-500 group-hover:scale-[1.04]"
            />
          ) : (
            <SinFoto />
          )}
        </div>

        <div className="flex flex-1 flex-col p-5">
          <p className="font-display text-[1.6rem] leading-tight font-extrabold tracking-tight text-gray-900 tabular-nums">
            {formatearPrecio(precio, moneda)}
          </p>
          {expensas > 0 && (
            <p className="mt-0.5 font-display text-sm text-gray-500 tabular-nums">+ {formatearPrecio(expensas, moneda)} de expensas</p>
          )}
          {expensas === 0 && <p className="mt-0.5 text-sm text-gray-500">Sin expensas</p>}

          {titulo && <h2 className="mt-3 font-sans text-base font-bold tracking-normal text-gray-900 line-clamp-2">{titulo}</h2>}

          <p className="mt-1 flex items-start gap-1 text-[15px] text-gray-600">
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="mt-0.5 h-4 w-4 shrink-0">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 21s-7-6.2-7-11.5a7 7 0 1 1 14 0C19 14.8 12 21 12 21Z" />
              <circle cx="12" cy="9.5" r="2.5" />
            </svg>
            <span className="line-clamp-1">{ubicacion}</span>
          </p>

          {(ambientes || metrosCuadrados) && (
            <ul aria-label="Características" className="mt-auto flex flex-wrap gap-1.5 pt-4">
              {ambientes && <li className="rounded-lg bg-gray-100 px-2.5 py-1 text-sm text-gray-800">{ambientes}</li>}
              {metrosCuadrados && (
                <li className="rounded-lg bg-gray-100 px-2.5 py-1 text-sm text-gray-800">{metrosCuadrados} m²</li>
              )}
            </ul>
          )}
        </div>
      </Card>
      {conFavorito && <BotonFavorito tipo="propiedad" id={id} className="absolute right-3 top-3" />}
    </div>
  )
}
