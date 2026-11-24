// Reglas de las imágenes de una propiedad (#77). Las sube POST /propiedades/{id}/imagenes (#59).
//
// Copia los defaults del backend (ImagenesProperties). Si cambian allá (app.imagenes.*), hay que
// cambiarlos acá. Igual el backend vuelve a validar y su mensaje se muestra si rechaza alguna.

export const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp']
export const TAMANIO_MAXIMO_MB = 5
export const MAXIMO_IMAGENES = 20

const TAMANIO_MAXIMO_BYTES = TAMANIO_MAXIMO_MB * 1024 * 1024

let siguienteId = 0

/** Imagen elegida por el usuario, todavía sin subir. `preview` es una URL local (hay que liberarla). */
export function crearImagenLocal(archivo) {
  siguienteId += 1
  return { id: `img-${siguienteId}`, archivo, preview: URL.createObjectURL(archivo) }
}

export function liberarPreview(imagen) {
  URL.revokeObjectURL(imagen.preview)
}

/** Mensaje de error si el archivo no se puede subir; null si está bien. */
export function errorArchivo(archivo) {
  if (!TIPOS_PERMITIDOS.includes(archivo.type)) return `${archivo.name}: solo se aceptan JPG, PNG o WEBP`
  if (archivo.size > TAMANIO_MAXIMO_BYTES) return `${archivo.name}: pesa más de ${TAMANIO_MAXIMO_MB} MB`
  return null
}

/**
 * Separa los archivos elegidos en los que se pueden agregar y los errores de los que no.
 * Respeta el máximo por propiedad contando las que ya estaban (`yaAgregadas`).
 */
export function filtrarArchivos(archivos, yaAgregadas) {
  const validos = []
  const errores = []
  for (const archivo of archivos) {
    const error = errorArchivo(archivo)
    if (error) errores.push(error)
    else validos.push(archivo)
  }
  const lugares = Math.max(0, MAXIMO_IMAGENES - yaAgregadas)
  if (validos.length > lugares) {
    errores.push(`Se pueden subir hasta ${MAXIMO_IMAGENES} imágenes: ${validos.length - lugares} quedaron afuera`)
  }
  return { validos: validos.slice(0, lugares), errores }
}

/** Copia de la lista con el elemento de `desde` movido a la posición `hasta`. */
export function mover(lista, desde, hasta) {
  if (desde === hasta || hasta < 0 || hasta >= lista.length) return lista
  const copia = [...lista]
  const [movido] = copia.splice(desde, 1)
  copia.splice(hasta, 0, movido)
  return copia
}
