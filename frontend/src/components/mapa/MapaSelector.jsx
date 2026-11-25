import 'leaflet/dist/leaflet.css'
import { useEffect, useMemo } from 'react'
import { MapContainer, Marker, TileLayer, useMap, useMapEvents } from 'react-leaflet'
import { CAPA_OSM, CENTRO_RIO_CUARTO, ZOOM_CALLE, ZOOM_CIUDAD } from '../../utils/mapa'
import { iconoPin } from './iconos'

/** Centra el mapa cuando llega una sugerencia nueva (no cuando el usuario arrastra el pin). */
function Centrar({ centro, zoom, version }) {
  const map = useMap()
  useEffect(() => {
    map.setView(centro, zoom)
    // eslint-disable-next-line react-hooks/exhaustive-deps -- solo al cambiar la sugerencia
  }, [map, version])
  return null
}

/** Tocar el mapa mueve el pin (o lo pone, si todavía no hay). */
function AlTocar({ onElegir }) {
  useMapEvents({
    click: (event) => onElegir({ latitud: event.latlng.lat, longitud: event.latlng.lng }),
  })
  return null
}

/**
 * Mapa del formulario de publicar (#141): pin arrastrable para confirmar la ubicación exacta.
 *
 * @param {{latitud, longitud} | null} props.ubicacion  pin actual (null = todavía sin marcar)
 * @param {number} props.version  cambia cuando llega una sugerencia nueva, para recentrar
 * @param {Function} props.onCambiar  recibe { latitud, longitud }
 */
export default function MapaSelector({ ubicacion, version, onCambiar }) {
  const centro = ubicacion ? [ubicacion.latitud, ubicacion.longitud] : CENTRO_RIO_CUARTO
  const zoom = ubicacion ? ZOOM_CALLE : ZOOM_CIUDAD

  const eventos = useMemo(
    () => ({
      dragend: (event) => {
        const { lat, lng } = event.target.getLatLng()
        onCambiar({ latitud: lat, longitud: lng })
      },
    }),
    [onCambiar],
  )

  return (
    <MapContainer
      center={centro}
      zoom={zoom}
      scrollWheelZoom={false}
      className="h-72 w-full rounded-lg"
      aria-label="Mapa para marcar la ubicación de la propiedad"
    >
      <TileLayer url={CAPA_OSM.url} attribution={CAPA_OSM.atribucion} />
      <Centrar centro={centro} zoom={zoom} version={version} />
      <AlTocar onElegir={onCambiar} />
      {ubicacion && (
        <Marker
          position={[ubicacion.latitud, ubicacion.longitud]}
          icon={iconoPin}
          draggable
          autoPan
          title="Ubicación de la propiedad: arrastrá el pin para corregirla"
          alt="Ubicación de la propiedad"
          eventHandlers={eventos}
        />
      )}
    </MapContainer>
  )
}
