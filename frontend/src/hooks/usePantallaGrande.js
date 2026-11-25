import { useSyncExternalStore } from 'react'

const CONSULTA = '(min-width: 1024px)' // breakpoint lg de Tailwind

function suscribir(avisar) {
  if (typeof window === 'undefined' || !window.matchMedia) return () => {}
  const media = window.matchMedia(CONSULTA)
  media.addEventListener('change', avisar)
  return () => media.removeEventListener('change', avisar)
}

const leer = () => (typeof window !== 'undefined' && window.matchMedia ? window.matchMedia(CONSULTA).matches : false)

/** true en pantallas de 1024 px o más (lista y mapa lado a lado en el listado). */
export function usePantallaGrande() {
  return useSyncExternalStore(suscribir, leer, () => false)
}
