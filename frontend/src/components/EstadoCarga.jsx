import { Button, CLASES_CARD } from './ui'

// Estados comunes de los listados (visitas, mis propiedades, favoritos): cargando, error con reintento y vacío
export function Cargando({ etiqueta }) {
  return (
    <ul role="status" aria-label={etiqueta} className="space-y-3">
      {[0, 1, 2].map((i) => (
        <li key={i} className={`h-24 animate-pulse ${CLASES_CARD}`} />
      ))}
    </ul>
  )
}

export function ErrorConReintento({ mensaje, onReintentar }) {
  return (
    <div className="py-12 text-center">
      <p role="alert" className="text-red-600">{mensaje}</p>
      <Button onClick={onReintentar} className="mt-4">
        Reintentar
      </Button>
    </div>
  )
}

// La acción es un link (`to`) o un botón (`onAccion`)
export function Vacio({ titulo, texto, accion, to, onAccion }) {
  return (
    <div className="rounded-lg border border-dashed border-gray-300 bg-white py-12 text-center">
      <p className="text-lg font-medium text-gray-900">{titulo}</p>
      {texto && <p className="mt-1 text-sm text-gray-500">{texto}</p>}
      {accion && (
        <Button to={to} onClick={onAccion} className="mt-4">
          {accion}
        </Button>
      )}
    </div>
  )
}
