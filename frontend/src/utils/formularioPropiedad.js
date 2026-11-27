// Reglas del formulario de publicar propiedad (#76). Consume POST /propiedades.
//
// Copia las validaciones del backend (PropiedadRequest) para avisar antes de enviar.
// Si cambian allá, hay que cambiarlas acá. Igual el backend vuelve a validar y sus
// errores se muestran en el campo que corresponde (ver erroresDelBackend).

import { esFechaValida, hoyISO } from './disponibilidad'

const LARGO_MAXIMO = 255 // columnas VARCHAR(255)
const ENTERO_MAXIMO = 2_147_483_647 // Integer de Java: más grande, Jackson rechaza el cuerpo entero
const DECIMALES_MONEDA = 2

export const MONEDAS = [
  { valor: 'ARS', label: 'Pesos ($)' },
  { valor: 'USD', label: 'Dólares (US$)' },
]

// El backend acepta true, false o null (no informado)
export const OPCIONES_IMPUESTOS = [
  { valor: '', label: 'No especificar' },
  { valor: 'si', label: 'Sí, incluye impuestos' },
  { valor: 'no', label: 'No, impuestos aparte' },
]

export const PROPIEDAD_VACIA = {
  titulo: '',
  descripcion: '',
  precio: '',
  moneda: 'ARS',
  expensas: '',
  incluyeImpuestos: '',
  plazoActualizacionMeses: '',
  calle: '',
  altura: '',
  piso: '',
  habitaciones: '',
  metrosCuadrados: '',
  disponibleDesde: '', // 'YYYY-MM-DD'; vacío = ya disponible (#154)
  requisitosIngreso: '',
}

const vacio = (valor) => String(valor ?? '').trim() === ''

function decimales(texto) {
  const [, parteDecimal = ''] = String(texto).split('.')
  return parteDecimal.length
}

function errorTexto(valor, { obligatorio, mensajeObligatorio }) {
  if (vacio(valor)) return obligatorio ? mensajeObligatorio : null
  if (valor.trim().length > LARGO_MAXIMO) return `Máximo ${LARGO_MAXIMO} caracteres`
  return null
}

function errorMonto(valor, { obligatorio, mensajeObligatorio, permiteCero }) {
  if (vacio(valor)) return obligatorio ? mensajeObligatorio : null
  const numero = Number(valor)
  if (Number.isNaN(numero)) return 'Ingresá un número válido'
  if (permiteCero ? numero < 0 : numero <= 0) return permiteCero ? 'No puede ser negativo' : 'Tiene que ser mayor a 0'
  if (decimales(valor) > DECIMALES_MONEDA) return `Usá como máximo ${DECIMALES_MONEDA} decimales`
  return null
}

function errorEntero(valor, { minimo, mensajeMinimo }) {
  if (vacio(valor)) return null
  const numero = Number(valor)
  if (!Number.isInteger(numero)) return 'Ingresá un número entero'
  if (numero < minimo) return mensajeMinimo
  if (numero > ENTERO_MAXIMO) return 'El número es demasiado grande'
  return null
}

/**
 * "Disponible desde" (#154): una fecha real y no anterior a hoy. Al editar se acepta la que ya tenía
 * aunque haya pasado (el backend hace lo mismo), así se pueden cambiar otros datos sin tocarla.
 */
function errorDisponibleDesde(valor, original) {
  if (vacio(valor)) return null
  if (!esFechaValida(valor)) return 'Ingresá una fecha válida'
  if (valor < hoyISO() && valor !== original) return 'No puede ser anterior a hoy'
  return null
}

/**
 * Errores por campo ({ campo: mensaje }); vacío si está todo bien.
 *
 * @param {object} [opciones.disponibleDesdeOriginal]  al editar, la fecha que la propiedad ya tenía
 */
export function validarPropiedad(datos, { disponibleDesdeOriginal = '' } = {}) {
  const errores = {
    titulo: errorTexto(datos.titulo, { obligatorio: true, mensajeObligatorio: 'Ingresá un título' }),
    precio: errorMonto(datos.precio, { obligatorio: true, mensajeObligatorio: 'Ingresá el precio' }),
    moneda: MONEDAS.some(({ valor }) => valor === datos.moneda) ? null : 'Elegí la moneda',
    expensas: errorMonto(datos.expensas, { permiteCero: true }),
    calle: errorTexto(datos.calle, { obligatorio: true, mensajeObligatorio: 'Ingresá la calle' }),
    altura: errorTexto(datos.altura, { obligatorio: true, mensajeObligatorio: 'Ingresá la altura' }),
    piso: errorTexto(datos.piso, { obligatorio: false }),
    habitaciones: errorEntero(datos.habitaciones, { minimo: 0, mensajeMinimo: 'No puede ser negativo' }),
    metrosCuadrados: errorEntero(datos.metrosCuadrados, { minimo: 1, mensajeMinimo: 'Tiene que ser mayor a 0' }),
    disponibleDesde: errorDisponibleDesde(datos.disponibleDesde, disponibleDesdeOriginal),
    plazoActualizacionMeses: errorEntero(datos.plazoActualizacionMeses, {
      minimo: 1,
      mensajeMinimo: 'Tiene que ser de al menos 1 mes',
    }),
  }
  return Object.fromEntries(Object.entries(errores).filter(([, mensaje]) => mensaje))
}

const textoONull = (valor) => (vacio(valor) ? null : valor.trim())
const numeroONull = (valor) => (vacio(valor) ? null : Number(valor))

/**
 * Cuerpo de POST /propiedades: textos recortados y los campos opcionales vacíos como null.
 * La ubicación (#141) es la que dejó el usuario con el pin; sin pin va null.
 */
export function armarPropiedadRequest(datos, ubicacion = null) {
  return {
    titulo: datos.titulo.trim(),
    descripcion: textoONull(datos.descripcion),
    precio: Number(datos.precio),
    moneda: datos.moneda,
    expensas: numeroONull(datos.expensas),
    incluyeImpuestos: datos.incluyeImpuestos === '' ? null : datos.incluyeImpuestos === 'si',
    plazoActualizacionMeses: numeroONull(datos.plazoActualizacionMeses),
    calle: datos.calle.trim(),
    altura: datos.altura.trim(),
    piso: textoONull(datos.piso),
    habitaciones: numeroONull(datos.habitaciones),
    metrosCuadrados: numeroONull(datos.metrosCuadrados),
    disponibleDesde: textoONull(datos.disponibleDesde),
    requisitosIngreso: textoONull(datos.requisitosIngreso),
    latitud: ubicacion?.latitud ?? null,
    longitud: ubicacion?.longitud ?? null,
  }
}

/**
 * Reparte los errores de validación del backend (400) entre los campos del formulario.
 * El backend los manda como "campo: mensaje" (GlobalExceptionHandler, #51).
 *
 * @returns {{ porCampo: object, otros: string[] }} lo que no corresponde a un campo conocido va en `otros`
 */
export function erroresDelBackend(detalles = []) {
  const porCampo = {}
  const otros = []
  for (const detalle of detalles ?? []) {
    const separador = String(detalle).indexOf(': ')
    const campo = separador > 0 ? detalle.slice(0, separador) : null
    if (campo && campo in PROPIEDAD_VACIA) {
      porCampo[campo] ??= detalle.slice(separador + 2)
    } else {
      otros.push(detalle)
    }
  }
  return { porCampo, otros }
}

const textoDe = (valor) => (valor === null || valor === undefined ? '' : String(valor))

/**
 * Datos del formulario a partir de una propiedad del backend (para editarla).
 * Es la inversa de armarPropiedadRequest: los null pasan a '' y los números a texto.
 */
export function datosDesdePropiedad(propiedad) {
  let incluyeImpuestos = ''
  if (propiedad.incluyeImpuestos === true) incluyeImpuestos = 'si'
  if (propiedad.incluyeImpuestos === false) incluyeImpuestos = 'no'

  return {
    titulo: textoDe(propiedad.titulo),
    descripcion: textoDe(propiedad.descripcion),
    precio: textoDe(propiedad.precio),
    moneda: MONEDAS.some(({ valor }) => valor === propiedad.moneda) ? propiedad.moneda : 'ARS',
    expensas: textoDe(propiedad.expensas),
    incluyeImpuestos,
    plazoActualizacionMeses: textoDe(propiedad.plazoActualizacionMeses),
    calle: textoDe(propiedad.calle),
    altura: textoDe(propiedad.altura),
    piso: textoDe(propiedad.piso),
    habitaciones: textoDe(propiedad.habitaciones),
    metrosCuadrados: textoDe(propiedad.metrosCuadrados),
    disponibleDesde: textoDe(propiedad.disponibleDesde),
    requisitosIngreso: textoDe(propiedad.requisitosIngreso),
  }
}

// PROPIEDAD_VACIA tiene los campos en el orden de la pantalla: al haber errores se enfoca el primero
const ORDEN_CAMPOS = Object.keys(PROPIEDAD_VACIA)

/** Enfoca el primer campo con error (los inputs tienen como id el nombre del campo). */
export function enfocarPrimerError(errores) {
  const campo = ORDEN_CAMPOS.find((clave) => errores[clave])
  if (campo) document.getElementById(campo)?.focus()
}
