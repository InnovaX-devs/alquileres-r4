import 'leaflet/dist/leaflet.css'
import { useEffect, useMemo } from 'react'
import { MapContainer, Marker, useMap } from 'react-leaflet'
import { useNavigate } from 'react-router-dom'
import L from 'leaflet'
import { CENTRO_RIO_CUARTO, ZOOM_CIUDAD, precioCorto, tieneUbicacion } from '../../utils/mapa'
import { iconoPrecio } from './iconos'
import CapaBase from './CapaBase'

/** Encuadra el mapa para que se vean todas las propiedades (o el centro si no hay ninguna). */
function Encuadre({ puntos }) {
  const map = useMap()
  const clave = puntos.map((p) => p.join(',')).join(';')
  useEffect(() => {
    if (puntos.length === 0) {
      map.setView(CENTRO_RIO_CUARTO, ZOOM_CIUDAD)
    } else if (puntos.length === 1) {
      map.setView(puntos[0], 15)
    } else {
      map.fitBounds(L.latLngBounds(puntos), { padding: [40, 40], maxZoom: 16 })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- se reencuadra solo si cambian los puntos
  }, [map, clave])
  return null
}

/**
 * Mapa del listado (#141): un pin con el precio de cada propiedad que tiene ubicación.
 * La ubicación es la aproximada que manda el backend (#140).
 * Pasar el mouse por un pin resalta su tarjeta y al revés (resaltadaId / onResaltar).
 * Tocar un pin lleva al detalle.
 */
export default function MapaPropiedades({ propiedades, resaltadaId, onResaltar, className = '' }) {
  const navigate = useNavigate()
  const conUbicacion = useMemo(() => propiedades.filter(tieneUbicacion), [propiedades])
  const puntos = useMemo(() => conUbicacion.map((p) => [p.latitud, p.longitud]), [conUbicacion])
  const sinUbicacion = propiedades.length - conUbicacion.length

  return (
    <div className={`relative overflow-hidden rounded-[22px] ${className}`}>
      <MapContainer
        center={CENTRO_RIO_CUARTO}
        zoom={ZOOM_CIUDAD}
        scrollWheelZoom
        className="h-full w-full"
        aria-label="Mapa de propiedades"
      >
        <CapaBase />
        <Encuadre puntos={puntos} />
        {conUbicacion.map((propiedad) => {
          const resaltada = propiedad.id === resaltadaId
          return (
            <Marker
              key={propiedad.id}
              position={[propiedad.latitud, propiedad.longitud]}
              icon={iconoPrecio(precioCorto(propiedad.precio, propiedad.moneda), resaltada)}
              zIndexOffset={resaltada ? 1000 : 0}
              title={propiedad.titulo || 'Ver propiedad'}
              alt={`${precioCorto(propiedad.precio, propiedad.moneda)}: ${propiedad.titulo || 'propiedad'}`}
              keyboard
              eventHandlers={{
                mouseover: () => onResaltar?.(propiedad.id),
                mouseout: () => onResaltar?.(null),
                click: () => navigate(`/propiedades/${propiedad.id}`),
              }}
            />
          )
        })}
      </MapContainer>

      {sinUbicacion > 0 && (
        <p className="pointer-events-none absolute right-3 top-3 z-[500] max-w-[calc(100%-5rem)] rounded-md bg-white/90 px-2.5 py-1 text-xs text-gray-700 shadow-sm">
          {sinUbicacion === propiedades.length
            ? 'Estas propiedades no tienen ubicación en el mapa'
            : `${sinUbicacion} ${sinUbicacion === 1 ? 'propiedad no tiene' : 'propiedades no tienen'} ubicación en el mapa`}
        </p>
      )}
    </div>
  )
}
