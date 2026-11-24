// Formato de los datos de una propiedad para mostrarlos en pantalla (#72).
// Lo usan las tarjetas del listado; sirve también para el detalle (#74).

const SIMBOLO_MONEDA = { ARS: '$', USD: 'US$' }

// Sin decimales si el precio es redondo; si no, siempre dos (1.200,50 y no 1.200,5)
const enteroAR = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const decimalAR = new Intl.NumberFormat('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

/** 350000, 'ARS' → "$ 350.000" · 1200, 'USD' → "US$ 1.200" · sin precio → "Consultar precio" */
export function formatearPrecio(precio, moneda) {
  const numero = Number(precio)
  if (precio === null || precio === undefined || Number.isNaN(numero)) return 'Consultar precio'
  const simbolo = SIMBOLO_MONEDA[moneda] ?? moneda ?? ''
  return `${simbolo} ${(Number.isInteger(numero) ? enteroAR : decimalAR).format(numero)}`.trim()
}

/** "Av. Corrientes 1234, piso 5B" · sin calle → "Ubicación no informada" */
export function formatearUbicacion({ calle, altura, piso }) {
  const direccion = [calle, altura].map((parte) => parte?.trim()).filter(Boolean).join(' ')
  if (!direccion) return 'Ubicación no informada'
  return piso?.trim() ? `${direccion}, piso ${piso.trim()}` : direccion
}

/** 0 → "Monoambiente" · 1 → "1 habitación" · 3 → "3 habitaciones" · sin dato → null */
export function formatearHabitaciones(habitaciones) {
  if (habitaciones === null || habitaciones === undefined) return null
  if (habitaciones === 0) return 'Monoambiente'
  return `${habitaciones} ${habitaciones === 1 ? 'habitación' : 'habitaciones'}`
}

/** 12 → "Actualiza cada 12 meses" · 1 → "Actualiza cada mes" · sin dato → null */
export function formatearPlazoActualizacion(meses) {
  if (!meses) return null
  return meses === 1 ? 'Actualiza cada mes' : `Actualiza cada ${meses} meses`
}

/** true → "Impuestos incluidos" · false → "Impuestos aparte" · sin dato → null */
export function formatearImpuestos(incluyeImpuestos) {
  if (incluyeImpuestos === null || incluyeImpuestos === undefined) return null
  return incluyeImpuestos ? 'Impuestos incluidos' : 'Impuestos aparte'
}
