import { ESTADOS_VISITA } from '../utils/visitas'

// Etiqueta de color con el estado de una solicitud de visita
export default function EstadoVisita({ estado }) {
  const info = ESTADOS_VISITA[estado] ?? { label: estado, clases: 'bg-gray-50 text-gray-700 border-gray-200' }
  return (
    <span className={`inline-block rounded-full border px-2.5 py-0.5 text-xs font-bold ${info.clases}`}>
      {info.label}
    </span>
  )
}
