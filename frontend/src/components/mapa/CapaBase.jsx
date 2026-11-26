import { TileLayer } from 'react-leaflet'
import { CAPA_MAPA } from '../../utils/mapa'

/** Mapa base (las "baldosas" de fondo) que comparten todos los mapas. Ver CAPA_MAPA. */
export default function CapaBase() {
  return (
    <TileLayer
      url={CAPA_MAPA.url}
      attribution={CAPA_MAPA.atribucion}
      {...(CAPA_MAPA.subdominios ? { subdomains: CAPA_MAPA.subdominios } : {})}
      maxZoom={CAPA_MAPA.zoomMaximo}
    />
  )
}
