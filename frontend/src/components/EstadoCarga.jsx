import { Link } from 'react-router-dom'

// Estados comunes de los listados (visitas, mis propiedades): cargando, error con reintento y vacío
export function Cargando({ etiqueta }) {
  return (
    <ul role="status" aria-label={etiqueta} className="space-y-3">
      {[0, 1, 2].map((i) => (
        <li key={i} className="h-24 animate-pulse rounded-lg border border-gray-200 bg-white" />
      ))}
    </ul>
  )
}

export function ErrorConReintento({ mensaje, onReintentar }) {
  return (
    <div className="py-12 text-center">
      <p role="alert" className="text-red-600">{mensaje}</p>
      <button
        onClick={onReintentar}
        className="mt-4 rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700"
      >
        Reintentar
      </button>
    </div>
  )
}

export function Vacio({ titulo, texto, accion, to }) {
  return (
    <div className="rounded-lg border border-dashed border-gray-300 bg-white py-12 text-center">
      <p className="text-lg font-medium text-gray-900">{titulo}</p>
      {texto && <p className="mt-1 text-sm text-gray-500">{texto}</p>}
      {accion && (
        <Link
          to={to}
          className="mt-4 inline-block rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          {accion}
        </Link>
      )}
    </div>
  )
}
