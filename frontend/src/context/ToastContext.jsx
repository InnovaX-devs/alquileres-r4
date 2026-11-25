import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { ToastContext } from './toastContextValue'

const DURACION_MS = { exito: 4000, info: 5000, error: 7000 }
const MAXIMO_VISIBLES = 3

const ESTILOS = {
  exito: { clases: 'border-green-200 bg-green-50 text-green-800', icono: '✓' },
  info: { clases: 'border-blue-200 bg-blue-50 text-blue-900', icono: 'i' },
  error: { clases: 'border-red-200 bg-red-50 text-red-700', icono: '!' },
}

const MENSAJE_ERROR_POR_DEFECTO = 'Ocurrió un error. Intentá de nuevo'

// Acepta un texto o un error (ApiError trae `mensaje`, que ya es claro para el usuario)
function textoDe(contenido, porDefecto) {
  if (typeof contenido === 'string') return contenido
  return contenido?.mensaje || porDefecto || MENSAJE_ERROR_POR_DEFECTO
}

function Toast({ toast, onCerrar }) {
  const { tipo, mensaje } = toast
  const { clases, icono } = ESTILOS[tipo]
  const [pausado, setPausado] = useState(false)

  // Se cierra solo; se pausa mientras el mouse o el foco están encima, para poder leerlo
  useEffect(() => {
    if (pausado) return
    const timer = setTimeout(onCerrar, DURACION_MS[tipo])
    return () => clearTimeout(timer)
  }, [pausado, tipo, onCerrar])

  return (
    <div
      role={tipo === 'error' ? 'alert' : 'status'}
      onMouseEnter={() => setPausado(true)}
      onMouseLeave={() => setPausado(false)}
      onFocus={() => setPausado(true)}
      onBlur={() => setPausado(false)}
      className={`pointer-events-auto flex items-start gap-3 rounded-md border px-4 py-3 text-sm shadow-lg ${clases}`}
    >
      <span
        aria-hidden="true"
        className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full border border-current text-xs font-bold"
      >
        {icono}
      </span>
      <p className="min-w-0 flex-1">{mensaje}</p>
      <button
        type="button"
        onClick={onCerrar}
        aria-label="Cerrar notificación"
        className="-m-1 shrink-0 rounded p-1 opacity-70 hover:opacity-100 focus:outline-none focus-visible:ring-2 focus-visible:ring-current"
      >
        <span aria-hidden="true">✕</span>
      </button>
    </div>
  )
}

/**
 * Notificaciones flotantes de éxito y error (#88). Se usan con el hook useToast.
 * Aparecen abajo (centradas en celulares, a la derecha en pantallas grandes) y se cierran solas.
 */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const siguienteId = useRef(0)

  const cerrar = useCallback((id) => {
    setToasts((actuales) => actuales.filter((t) => t.id !== id))
  }, [])

  const mostrar = useCallback((tipo, mensaje) => {
    const id = ++siguienteId.current
    setToasts((actuales) => {
      // El mismo mensaje dos veces seguidas (doble click, varios errores iguales) no se repite
      const sinRepetido = actuales.filter((t) => !(t.tipo === tipo && t.mensaje === mensaje))
      return [...sinRepetido, { id, tipo, mensaje }].slice(-MAXIMO_VISIBLES)
    })
  }, [])

  const valor = useMemo(
    () => ({
      exito: (mensaje) => mostrar('exito', mensaje),
      info: (mensaje) => mostrar('info', mensaje),
      error: (error, porDefecto) => mostrar('error', textoDe(error, porDefecto)),
    }),
    [mostrar],
  )

  return (
    <ToastContext.Provider value={valor}>
      {children}
      {/* Cada notificación es su propia región viva: role="alert" (errores) o role="status" */}
      <div className="pointer-events-none fixed inset-x-0 bottom-0 z-50 flex justify-center p-4 sm:justify-end">
        <div className="flex w-full max-w-sm flex-col gap-2">
          {toasts.map((toast) => (
            <ToastConCierre key={toast.id} toast={toast} onCerrar={cerrar} />
          ))}
        </div>
      </div>
    </ToastContext.Provider>
  )
}

// onCerrar estable por toast, para que el timer de Toast no se reinicie en cada render
function ToastConCierre({ toast, onCerrar }) {
  const cerrarEste = useCallback(() => onCerrar(toast.id), [onCerrar, toast.id])
  return <Toast toast={toast} onCerrar={cerrarEste} />
}
