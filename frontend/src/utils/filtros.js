// Filtros del listado de propiedades (#73). Los acepta GET /propiedades (#58): todos opcionales y combinables.

export const CLAVES_FILTROS = ['precioMin', 'precioMax', 'moneda', 'habitacionesMin', 'calle']

export const FILTROS_VACIOS = Object.fromEntries(CLAVES_FILTROS.map((clave) => [clave, '']))

export function hayFiltrosActivos(filtros) {
  return CLAVES_FILTROS.some((clave) => String(filtros[clave] ?? '').trim() !== '')
}

/** Mensaje de error si los precios no son válidos; null si están bien (o vacíos). */
export function validarFiltros({ precioMin, precioMax }) {
  const min = precioMin === '' ? null : Number(precioMin)
  const max = precioMax === '' ? null : Number(precioMax)
  if ((min !== null && min < 0) || (max !== null && max < 0)) return 'Los precios no pueden ser negativos'
  if (min !== null && max !== null && min > max) return 'El precio mínimo no puede ser mayor al máximo'
  return null
}
