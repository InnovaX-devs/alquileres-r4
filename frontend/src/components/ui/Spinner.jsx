const TAMANIOS = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-10 w-10',
}

/**
 * Indicador de carga (#87).
 *
 * @param {'sm'|'md'|'lg'} [props.tamanio='md']
 * @param {string} [props.etiqueta]  texto para lectores de pantalla. Sin etiqueta el spinner es
 *                                   decorativo (por ejemplo dentro de un botón que ya dice "Guardando...").
 */
export default function Spinner({ tamanio = 'md', etiqueta, className = '' }) {
  const icono = (
    <svg
      aria-hidden="true"
      viewBox="0 0 24 24"
      fill="none"
      className={`animate-spin ${TAMANIOS[tamanio]} ${className}`}
    >
      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" className="opacity-25" />
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
    </svg>
  )

  if (!etiqueta) return icono
  return (
    <span role="status" className="inline-flex">
      {icono}
      <span className="sr-only">{etiqueta}</span>
    </span>
  )
}

/** Spinner centrado para cuando carga una pantalla entera. */
export function CargandoPagina({ etiqueta = 'Cargando...' }) {
  return (
    <div className="flex justify-center py-16 text-blue-600">
      <Spinner tamanio="lg" etiqueta={etiqueta} />
    </div>
  )
}
