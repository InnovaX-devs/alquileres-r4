// Reglas del formulario de crear alerta (#85). Consume POST /alertas.
//
// Copia las validaciones del backend (AlertaRequest). Si cambian allá, hay que cambiarlas acá.
// Todos los campos son opcionales, pero el backend pide al menos un criterio (la moneda sola no
// alcanza) y la moneda cuando hay precio.

import { formatearPrecio } from './propiedad'

const LARGO_MAXIMO = 255 // columna VARCHAR(255)
const ENTERO_MAXIMO = 2_147_483_647 // Integer de Java

export const CLAVES_ALERTA = ['precioMin', 'precioMax', 'moneda', 'habitacionesMin', 'calle']

export const ALERTA_VACIA = Object.fromEntries(CLAVES_ALERTA.map((clave) => [clave, '']))

export const SIN_CRITERIOS = 'Completá al menos un criterio: precio, habitaciones o calle'

const vacio = (valor) => String(valor ?? '').trim() === ''

/** Valores iniciales desde la URL (?precioMax=500000&moneda=ARS), por ejemplo los filtros del listado. */
export function alertaDesdeParametros(searchParams) {
  return Object.fromEntries(CLAVES_ALERTA.map((clave) => [clave, searchParams.get(clave) ?? '']))
}

function errorPrecio(valor) {
  if (vacio(valor)) return null
  const numero = Number(valor)
  if (Number.isNaN(numero)) return 'Ingresá un número válido'
  if (numero < 0) return 'No puede ser negativo'
  if ((String(valor).split('.')[1] ?? '').length > 2) return 'Usá como máximo 2 decimales'
  return null
}

/**
 * @returns {{ porCampo: object, general: string | null }} errores por campo y el error que no es
 *          de un campo (no completó ningún criterio). Todo vacío/null si está bien.
 */
export function validarAlerta(datos) {
  const porCampo = {}

  const errorMin = errorPrecio(datos.precioMin)
  const errorMax = errorPrecio(datos.precioMax)
  if (errorMin) porCampo.precioMin = errorMin
  if (errorMax) porCampo.precioMax = errorMax
  if (!errorMin && !errorMax && !vacio(datos.precioMin) && !vacio(datos.precioMax)) {
    if (Number(datos.precioMin) > Number(datos.precioMax)) {
      porCampo.precioMax = 'Tiene que ser mayor o igual al mínimo'
    }
  }

  const hayPrecio = !vacio(datos.precioMin) || !vacio(datos.precioMax)
  if (hayPrecio && vacio(datos.moneda)) porCampo.moneda = 'Elegí la moneda del precio'

  if (!vacio(datos.habitacionesMin)) {
    const habitaciones = Number(datos.habitacionesMin)
    if (!Number.isInteger(habitaciones)) porCampo.habitacionesMin = 'Ingresá un número entero'
    else if (habitaciones < 1) porCampo.habitacionesMin = 'Tiene que ser al menos 1'
    else if (habitaciones > ENTERO_MAXIMO) porCampo.habitacionesMin = 'El número es demasiado grande'
  }

  if (datos.calle.trim().length > LARGO_MAXIMO) porCampo.calle = `Máximo ${LARGO_MAXIMO} caracteres`

  const hayCriterio = hayPrecio || !vacio(datos.habitacionesMin) || !vacio(datos.calle)
  return { porCampo, general: hayCriterio ? null : SIN_CRITERIOS }
}

/** Cuerpo de POST /alertas: los campos vacíos van como null. */
export function armarAlertaRequest(datos) {
  const numeroONull = (valor) => (vacio(valor) ? null : Number(valor))
  return {
    precioMin: numeroONull(datos.precioMin),
    precioMax: numeroONull(datos.precioMax),
    moneda: vacio(datos.moneda) ? null : datos.moneda,
    habitacionesMin: numeroONull(datos.habitacionesMin),
    calle: vacio(datos.calle) ? null : datos.calle.trim(),
  }
}

// Las validaciones cruzadas del backend (@AssertTrue) llegan con el nombre del método, no del campo
const CAMPO_DE_VALIDACION = {
  monedaConPrecio: 'moneda',
  rangoPrecioValido: 'precioMax',
}

/**
 * Reparte los errores de validación del backend (400, "campo: mensaje") entre los campos.
 * "criterios" (no mandó ninguno) y lo que no es de un campo conocido van en `otros`.
 */
export function erroresDelBackend(detalles = []) {
  const porCampo = {}
  const otros = []
  for (const detalle of detalles ?? []) {
    const separador = String(detalle).indexOf(': ')
    const nombre = separador > 0 ? detalle.slice(0, separador) : null
    const campo = CAMPO_DE_VALIDACION[nombre] ?? nombre
    if (campo && CLAVES_ALERTA.includes(campo)) porCampo[campo] ??= detalle.slice(separador + 2)
    else otros.push(separador > 0 ? detalle.slice(separador + 2) : detalle)
  }
  return { porCampo, otros }
}

/** Resumen legible de una alerta (respuesta del backend): ["Hasta $ 500.000", "2+ habitaciones", ...] */
export function describirAlerta({ precioMin, precioMax, moneda, habitacionesMin, calle }) {
  const partes = []
  const tieneMin = precioMin !== null && precioMin !== undefined
  const tieneMax = precioMax !== null && precioMax !== undefined
  if (tieneMin && tieneMax) {
    partes.push(`Entre ${formatearPrecio(precioMin, moneda)} y ${formatearPrecio(precioMax, moneda)}`)
  } else if (tieneMin) {
    partes.push(`Desde ${formatearPrecio(precioMin, moneda)}`)
  } else if (tieneMax) {
    partes.push(`Hasta ${formatearPrecio(precioMax, moneda)}`)
  } else if (moneda) {
    partes.push(moneda === 'USD' ? 'En dólares' : 'En pesos')
  }
  if (habitacionesMin) {
    partes.push(`${habitacionesMin} ${habitacionesMin === 1 ? 'habitación' : 'habitaciones'} o más`)
  }
  if (calle) partes.push(`En calle ${calle}`)
  return partes
}
