import { lazy, Suspense } from 'react'

// Los mapas (Leaflet, ~150 KB) se cargan recién cuando se muestran, para no agrandar la
// carga inicial de la app (#141).
const MapaPropiedadesDiferido = lazy(() => import('./MapaPropiedades'))
const MapaZonaDiferido = lazy(() => import('./MapaZona'))
const MapaSelectorDiferido = lazy(() => import('./MapaSelector'))

function CargandoMapa({ className = '' }) {
  return (
    <div
      role="status"
      aria-label="Cargando mapa"
      className={`flex animate-pulse items-center justify-center rounded-xl bg-gray-200 text-sm text-gray-500 ${className}`}
    >
      Cargando mapa...
    </div>
  )
}

export function MapaPropiedades(props) {
  return (
    <Suspense fallback={<CargandoMapa className={props.className} />}>
      <MapaPropiedadesDiferido {...props} />
    </Suspense>
  )
}

export function MapaZona(props) {
  return (
    <Suspense fallback={<CargandoMapa className="h-56" />}>
      <MapaZonaDiferido {...props} />
    </Suspense>
  )
}

export function MapaSelector(props) {
  return (
    <Suspense fallback={<CargandoMapa className="h-72" />}>
      <MapaSelectorDiferido {...props} />
    </Suspense>
  )
}
