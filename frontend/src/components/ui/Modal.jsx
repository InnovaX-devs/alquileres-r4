import { useEffect, useId, useRef } from 'react'
import { createPortal } from 'react-dom'

const ANCHOS = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-lg',
}

const FOCUSABLES =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

/**
 * Ventana modal (#87). Se muestra mientras está montada: el que la usa decide cuándo renderizarla.
 *
 * - Se cierra con Escape, con la X o tocando afuera.
 * - El foco queda adentro mientras está abierta (Tab no se escapa a la página de atrás) y al
 *   cerrarse vuelve al elemento que la abrió.
 * - La página de atrás no scrollea.
 * - Si ningún elemento de adentro tiene autoFocus, el foco va al primer campo o botón.
 *
 * @param {node}     props.titulo
 * @param {node}     [props.subtitulo]
 * @param {Function} props.onCerrar
 * @param {'sm'|'md'|'lg'} [props.ancho='md']
 */
export default function Modal({ titulo, subtitulo, onCerrar, ancho = 'md', children }) {
  const idTitulo = useId()
  const ventana = useRef(null)

  // Siempre la última versión de onCerrar, sin volver a registrar los eventos en cada render
  const onCerrarRef = useRef(onCerrar)
  useEffect(() => {
    onCerrarRef.current = onCerrar
  }, [onCerrar])

  useEffect(() => {
    const anterior = document.activeElement
    const overflowAnterior = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    // Foco inicial: lo que tenga autoFocus (React ya lo enfocó) o el primer elemento enfocable
    if (!ventana.current.contains(document.activeElement)) {
      const enfocables = ventana.current.querySelectorAll(FOCUSABLES)
      // El primero es la X: se prefiere el primer elemento del contenido si existe
      ;(enfocables[1] ?? enfocables[0])?.focus()
    }

    const alPresionar = (event) => {
      if (event.key === 'Escape') {
        event.stopPropagation()
        onCerrarRef.current()
        return
      }
      if (event.key !== 'Tab') return
      const enfocables = [...ventana.current.querySelectorAll(FOCUSABLES)]
      if (enfocables.length === 0) return
      const primero = enfocables[0]
      const ultimo = enfocables[enfocables.length - 1]
      if (event.shiftKey && document.activeElement === primero) {
        event.preventDefault()
        ultimo.focus()
      } else if (!event.shiftKey && document.activeElement === ultimo) {
        event.preventDefault()
        primero.focus()
      }
    }
    document.addEventListener('keydown', alPresionar)

    return () => {
      document.removeEventListener('keydown', alPresionar)
      document.body.style.overflow = overflowAnterior
      anterior?.focus?.()
    }
  }, [])

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onCerrar()
      }}
    >
      <div
        ref={ventana}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        className={`max-h-full w-full overflow-y-auto rounded-lg bg-white p-6 shadow-xl ${ANCHOS[ancho]}`}
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 id={idTitulo} className="text-lg font-semibold text-gray-900">
              {titulo}
            </h2>
            {subtitulo && <p className="mt-0.5 text-sm text-gray-500">{subtitulo}</p>}
          </div>
          <button
            type="button"
            onClick={onCerrar}
            aria-label="Cerrar ventana"
            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
          >
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5">
              <path strokeLinecap="round" d="M6 6l12 12M18 6 6 18" />
            </svg>
          </button>
        </div>
        {children}
      </div>
    </div>,
    document.body,
  )
}
