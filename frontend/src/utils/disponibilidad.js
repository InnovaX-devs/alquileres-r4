// "Disponible desde" de una propiedad (#154).
//
// Las fechas viajan como "YYYY-MM-DD" (el LocalDate del backend). No se pasan por new Date('YYYY-MM-DD'):
// eso se interpreta en UTC y en Argentina mostraría el día anterior. Se comparan como texto (con este
// formato ordena igual que las fechas) y, para mostrarlas, la fecha se arma en hora local.

const FORMATO_FECHA = /^(\d{4})-(\d{2})-(\d{2})$/

/** Hoy como "YYYY-MM-DD", en la hora del dispositivo. */
export function hoyISO(ahora = new Date()) {
  const mes = String(ahora.getMonth() + 1).padStart(2, '0')
  const dia = String(ahora.getDate()).padStart(2, '0')
  return `${ahora.getFullYear()}-${mes}-${dia}`
}

/** true si el texto es una fecha "YYYY-MM-DD" real (rechaza 2026-02-30). */
export function esFechaValida(texto) {
  const partes = FORMATO_FECHA.exec(texto ?? '')
  if (!partes) return false
  const [, anio, mes, dia] = partes.map(Number)
  const fecha = new Date(anio, mes - 1, dia)
  return fecha.getFullYear() === anio && fecha.getMonth() === mes - 1 && fecha.getDate() === dia
}

/** Se puede entrar ya: sin fecha, o la fecha es hoy o ya pasó. */
export function disponibleYa(disponibleDesde, hoy = hoyISO()) {
  return !disponibleDesde || !esFechaValida(disponibleDesde) || disponibleDesde <= hoy
}

/**
 * "Disponible ya" · "Disponible desde el 1 de marzo" (con el año si no es el actual).
 * Sin fecha, o con una que es hoy o ya pasó, es "Disponible ya".
 */
export function formatearDisponibilidad(disponibleDesde, hoy = hoyISO()) {
  if (disponibleYa(disponibleDesde, hoy)) return 'Disponible ya'
  const [anio, mes, dia] = disponibleDesde.split('-').map(Number)
  const opciones = { day: 'numeric', month: 'long' }
  if (String(anio) !== hoy.slice(0, 4)) opciones.year = 'numeric'
  return `Disponible desde el ${new Date(anio, mes - 1, dia).toLocaleDateString('es-AR', opciones)}`
}
