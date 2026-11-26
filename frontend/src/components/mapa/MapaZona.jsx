import 'leaflet/dist/leaflet.css'
import { Circle, MapContainer, TileLayer } from 'react-leaflet'
import { CAPA_OSM } from '../../utils/mapa'

/**
 * Mapa chico del detalle (#141): un círculo con la zona aproximada, no un pin exacto.
 * El backend ya manda el centro aproximado y el radio (#140), así no se puede deducir la casa.
 */
export default function MapaZona({ latitud, longitud, radioMetros = 150 }) {
  return (
    <MapContainer
      center={[latitud, longitud]}
      zoom={15}
      scrollWheelZoom={false}
      className="h-56 w-full"
      aria-label="Mapa con la zona aproximada de la propiedad"
    >
      <TileLayer url={CAPA_OSM.url} attribution={CAPA_OSM.atribucion} />
      <Circle
        center={[latitud, longitud]}
        radius={radioMetros}
        pathOptions={{ color: '#23665b', fillColor: '#3c9282', fillOpacity: 0.25, weight: 2 }}
      />
    </MapContainer>
  )
}
