// Compartir una propiedad por WhatsApp (#151).
//
// Se comparte el link del back (GET /compartir/propiedades/{id}) y no el de la SPA: WhatsApp no
// ejecuta JavaScript, así que solo ese HTML le puede dar la foto, el precio y la dirección para la
// vista previa. A la persona que lo abre, el back la redirige enseguida a /propiedades/{id}.

import { API_URL } from '../config/env'
import { formatearPrecio, formatearUbicacion } from './propiedad'

/** Link con vista previa para compartir. */
export function linkParaCompartir(id) {
  return `${API_URL}/compartir/propiedades/${id}`
}

/** "Depto centro · $ 350.000 por mes" (o la dirección si no tiene título). */
export function textoParaCompartir(propiedad) {
  const nombre = propiedad.titulo?.trim() || formatearUbicacion(propiedad)
  const precio = formatearPrecio(propiedad.precio, propiedad.moneda)
  const conPrecio = precio === 'Consultar precio' ? precio : `${precio} por mes`
  return `${nombre} · ${conPrecio}`
}

/** Link de WhatsApp con el texto y el link: lo usa la compu (wa.me abre WhatsApp Web o la app). */
export function linkWhatsApp(propiedad) {
  const texto = `${textoParaCompartir(propiedad)}\n${linkParaCompartir(propiedad.id)}`
  return `https://wa.me/?text=${encodeURIComponent(texto)}`
}

/**
 * En el celular se usa la hoja de compartir del sistema. En la compu no, aunque el navegador tenga
 * navigator.share (Chrome y Safari en escritorio lo tienen): ahí se abre WhatsApp directo.
 */
export function usarHojaDelSistema() {
  if (typeof navigator === 'undefined' || typeof navigator.share !== 'function') return false
  const tactil = typeof window !== 'undefined' && window.matchMedia?.('(pointer: coarse)').matches
  return Boolean(tactil)
}
