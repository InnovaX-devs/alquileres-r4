// Helpers de las solicitudes de visita (#79, #80, #81)

export const ESTADOS_VISITA = {
  PENDIENTE: { label: 'Pendiente', clases: 'bg-yellow-50 text-yellow-800 border-yellow-200' },
  ACEPTADA: { label: 'Aceptada', clases: 'bg-green-50 text-green-700 border-green-200' },
  RECHAZADA: { label: 'Rechazada', clases: 'bg-red-50 text-red-700 border-red-200' },
}

// Hora en formato 24 h (15:00 y no 3:00 p. m.)
const formatoFecha = new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short', hourCycle: 'h23' })

/**
 * El backend manda fechas LocalDateTime sin zona ('2026-11-25T15:00:00'): se interpretan como hora local.
 * '2026-11-25T15:00:00' → "25 nov 2026, 15:00"
 */
export function formatearFechaHora(valor) {
  if (!valor) return '—'
  const fecha = new Date(valor)
  return Number.isNaN(fecha.getTime()) ? '—' : formatoFecha.format(fecha)
}

const dosDigitos = (n) => String(n).padStart(2, '0')

/** Fecha en el formato del <input type="datetime-local">: 'YYYY-MM-DDTHH:mm' (hora local). */
export function aInputFechaHora(fecha) {
  return `${fecha.getFullYear()}-${dosDigitos(fecha.getMonth() + 1)}-${dosDigitos(fecha.getDate())}T${dosDigitos(
    fecha.getHours(),
  )}:${dosDigitos(fecha.getMinutes())}`
}

/** Mensaje de error de la fecha propuesta, o null si es válida (tiene que ser futura). */
export function validarFechaPropuesta(valor, ahora = new Date()) {
  if (!valor) return 'Elegí una fecha y hora para la visita'
  const fecha = new Date(valor)
  if (Number.isNaN(fecha.getTime())) return 'La fecha no es válida'
  if (fecha <= ahora) return 'La fecha tiene que ser futura'
  return null
}
