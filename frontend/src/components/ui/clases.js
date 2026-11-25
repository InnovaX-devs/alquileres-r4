// Clases de Tailwind de los componentes comunes (#87), para los casos que no pueden usar el
// componente (por ejemplo un esqueleto de carga con la forma de una tarjeta). Están en un archivo
// aparte porque los .jsx de componentes solo deben exportar componentes (Fast Refresh).
// Colores: ver la identidad visual en index.css (gray = piedra/pino, rio, sol).

/** Tarjeta (Card). */
export const CLASES_CARD = 'rounded-2xl border border-gray-200 bg-white'

const BASE_BOTON =
  'inline-flex items-center justify-center gap-2 rounded-xl font-bold transition ' +
  'focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600 focus-visible:ring-offset-2 ' +
  'active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60 disabled:active:scale-100'

const VARIANTES_BOTON = {
  // Amarillo sol: la acción principal de la pantalla (una por pantalla, idealmente)
  primario: 'bg-sol-400 text-pino-800 hover:bg-sol-500',
  // Verde pino: acciones importantes que no son la principal
  oscuro: 'bg-pino-800 text-white hover:bg-pino-700',
  secundario: 'border-2 border-gray-200 bg-white text-gray-900 hover:border-gray-300 hover:bg-gray-50',
  fantasma: 'text-gray-700 hover:bg-gray-200/60',
  suave: 'text-rio-700 hover:bg-rio-50',
  exito: 'bg-rio-600 text-white hover:bg-rio-700',
  advertencia: 'bg-sol-500 text-pino-800 hover:bg-sol-600',
  peligro: 'bg-red-600 text-white hover:bg-red-700',
  peligroSuave: 'text-red-700 hover:bg-red-50',
}

const TAMANIOS_BOTON = {
  sm: 'min-h-10 px-3.5 py-1.5 text-sm',
  md: 'min-h-11 px-5 py-2 text-[15px]',
  lg: 'min-h-14 px-7 py-3 text-base',
}

/** Botón (Button). */
export function clasesBoton({ variante = 'primario', tamanio = 'md', anchoCompleto = false, className = '' } = {}) {
  return `${BASE_BOTON} ${VARIANTES_BOTON[variante]} ${TAMANIOS_BOTON[tamanio]} ${anchoCompleto ? 'w-full' : ''} ${className}`.trim()
}

const CLASE_CONTROL =
  'mt-1.5 block w-full rounded-xl border-2 bg-white px-3.5 py-2.5 text-[15px] text-gray-900 ' +
  'placeholder:text-gray-400 transition-colors focus:outline-none focus:ring-3 ' +
  'disabled:bg-gray-100 disabled:text-gray-500'

/** Control de formulario (Input, Select, Textarea), según tenga error o no. */
export function clasesControl(error, className = '') {
  const estado = error
    ? 'border-red-400 focus:border-red-500 focus:ring-red-100'
    : 'border-gray-200 hover:border-gray-300 focus:border-rio-600 focus:ring-rio-100'
  return `${CLASE_CONTROL} ${estado} ${className}`.trim()
}
