import { useState } from 'react'
import { Link } from 'react-router-dom'
import BotonFavorito from './BotonFavorito'
import { formatearHabitaciones, formatearPrecio, formatearUbicacion } from '../utils/propiedad'

// Fondo de la foto mientras carga o si no hay: alterna coral, verde agua y arena
const TONOS_FOTO = ['bg-sol-200', 'bg-rio-100', 'bg-gray-200']

function SinFoto() {
  return (
    <div className="flex h-full w-full flex-col items-center justify-center gap-1 text-pino-800/60">
      <svg aria-hidden="true" viewBox="0 0 100 100" fill="none" className="h-14 w-14">
        <path d="M22 80V45L50 22l28 23v35" stroke="currentColor" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      <span className="text-xs">Sin fotos</span>
    </div>
  )
}

/** "Marta · dueño directo" o el nombre de la inmobiliaria. Solo si el listado trae el contacto. */
function Publicador({ contacto, tono }) {
  const nombre = contacto?.nombre?.trim()
  if (!nombre) return null
  const texto = contacto.tipo === 'INMOBILIARIA' ? nombre : `${nombre} · dueño directo`
  return (
    <p className="mt-3.5 flex items-center gap-2 text-sm text-gray-600">
      <span
        aria-hidden="true"
        className={`inline-flex h-[26px] w-[26px] shrink-0 items-center justify-center rounded-full text-xs font-bold text-pino-800 ${tono}`}
      >
        {nombre[0].toUpperCase()}
      </span>
      <span className="line-clamp-1">{texto}</span>
    </p>
  )
}

/**
 * Tarjeta del listado público (#72): foto, precio con expensas, título y ubicación, detalle y
 * quién publica. Sin caja: la foto redondeada y el texto debajo.
 * Toda la tarjeta es un link al detalle de la propiedad; el corazón de favorito (#82) va encima.
 *
 * @param {object} props.propiedad  un elemento de GET /propiedades
 * @param {boolean} [props.conFavorito=true]  false en Mis propiedades (#78): no se guardan las propias
 * @param {boolean} [props.resaltada]  su pin está resaltado en el mapa del listado (#141)
 * @param {Function} [props.onResaltar]  recibe el id al pasar el mouse o enfocar la tarjeta, y null al salir
 *
 * Si `propiedad.estado` es ALQUILADA lleva un cartel (se ve en Mis propiedades y en Favoritos).
 */
export default function TarjetaPropiedad({ propiedad, conFavorito = true, resaltada = false, onResaltar }) {
  const { id, titulo, precio, moneda, expensas, habitaciones, metrosCuadrados, imagenes, estado } = propiedad
  const alquilada = estado === 'ALQUILADA'
  const foto = imagenes?.[0]
  const [fotoRota, setFotoRota] = useState(false)

  const ubicacion = formatearUbicacion(propiedad)
  const detalle = [formatearHabitaciones(habitaciones), metrosCuadrados && `${metrosCuadrados} m²`]
    .filter(Boolean)
    .join(' · ')
  const tono = TONOS_FOTO[(Number(id) || 0) % TONOS_FOTO.length]

  return (
    <div
      className="relative h-full"
      onMouseEnter={onResaltar ? () => onResaltar(id) : undefined}
      onMouseLeave={onResaltar ? () => onResaltar(null) : undefined}
      onFocus={onResaltar ? () => onResaltar(id) : undefined}
      onBlur={onResaltar ? () => onResaltar(null) : undefined}
    >
      <Link
        to={`/propiedades/${id}`}
        className="group block h-full rounded-[18px] focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600 focus-visible:ring-offset-4 focus-visible:ring-offset-gray-100"
      >
        <div
          className={`relative aspect-[5/4] overflow-hidden rounded-[18px] transition ${tono} ${
            resaltada ? 'ring-2 ring-sol-400 ring-offset-2 ring-offset-gray-100' : ''
          }`}
        >
          {foto && !fotoRota ? (
            <img
              src={foto}
              alt={titulo ? `Foto de ${titulo}` : `Foto de la propiedad en ${ubicacion}`}
              loading="lazy"
              onError={() => setFotoRota(true)}
              className={`h-full w-full object-cover transition duration-500 group-hover:scale-[1.04] ${alquilada ? 'grayscale-[60%]' : ''}`}
            />
          ) : (
            <SinFoto />
          )}
          {alquilada && (
            <span className="absolute left-3 top-3 rounded-lg bg-pino-900/85 px-2.5 py-1 text-sm font-bold text-white">
              Alquilada
            </span>
          )}
        </div>

        <p className="mt-4 font-display text-2xl font-bold leading-tight tracking-[-0.03em] text-gray-900 tabular-nums">
          {formatearPrecio(precio, moneda)}{' '}
          {expensas > 0 && (
            <span className="font-sans text-[15px] font-normal tracking-normal text-gray-600">
              + {formatearPrecio(expensas, moneda)} de expensas
            </span>
          )}
          {expensas === 0 && <span className="font-sans text-[15px] font-normal tracking-normal text-gray-600">sin expensas</span>}
        </p>

        <div className="mt-1.5 line-clamp-2 text-base text-gray-900">
          {titulo && (
            <>
              <h2 className="inline font-sans text-base font-normal tracking-normal">{titulo}</h2>,{' '}
            </>
          )}
          <span>{ubicacion}</span>
        </div>

        {detalle && <p className="mt-0.5 text-[15px] text-gray-600">{detalle}</p>}

        <Publicador contacto={propiedad.contacto} tono={tono} />
      </Link>
      {conFavorito && <BotonFavorito tipo="propiedad" id={id} className="absolute right-2.5 top-2.5" />}
    </div>
  )
}
