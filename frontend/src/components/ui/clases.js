// Clases de Tailwind de los componentes comunes (#87), para los casos que no pueden usar el
// componente (por ejemplo un esqueleto de carga con la forma de una tarjeta). Están en un archivo
// aparte porque los .jsx de componentes solo deben exportar componentes (Fast Refresh).

/** Tarjeta (Card). */
export const CLASES_CARD = 'rounded-lg border border-gray-200 bg-white shadow-sm'

const BASE_BOTON =
  'inline-flex items-center justify-center gap-2 rounded-md font-medium transition-colors ' +
  'focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 ' +
  'disabled:cursor-not-allowed disabled:opacity-60'

const VARIANTES_BOTON = {
  primario: 'bg-blue-600 text-white hover:bg-blue-700',
  secundario: 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50',
  fantasma: 'text-gray-700 hover:bg-gray-100',
  suave: 'text-blue-600 hover:bg-blue-50',
  exito: 'bg-green-600 text-white hover:bg-green-700',
  advertencia: 'bg-yellow-600 text-white hover:bg-yellow-700',
  peligro: 'bg-red-600 text-white hover:bg-red-700',
  peligroSuave: 'text-red-600 hover:bg-red-50',
}

const TAMANIOS_BOTON = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2 text-sm',
}

/** Botón (Button). */
export function clasesBoton({ variante = 'primario', tamanio = 'md', anchoCompleto = false, className = '' } = {}) {
  return `${BASE_BOTON} ${VARIANTES_BOTON[variante]} ${TAMANIOS_BOTON[tamanio]} ${anchoCompleto ? 'w-full' : ''} ${className}`.trim()
}

const CLASE_CONTROL =
  'mt-1 block w-full rounded-md border bg-white px-3 py-2 text-sm text-gray-900 shadow-sm ' +
  'placeholder:text-gray-400 focus:outline-none focus:ring-2 disabled:bg-gray-100 disabled:text-gray-500'

/** Control de formulario (Input, Select, Textarea), según tenga error o no. */
export function clasesControl(error, className = '') {
  const estado = error
    ? 'border-red-400 focus:border-red-500 focus:ring-red-200'
    : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200'
  return `${CLASE_CONTROL} ${estado} ${className}`.trim()
}
