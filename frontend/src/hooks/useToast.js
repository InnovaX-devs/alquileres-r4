import { useContext } from 'react'
import { ToastContext } from '../context/toastContextValue'

/**
 * Notificaciones flotantes (#88) para el resultado de una acción: guardar, eliminar, aceptar...
 *
 *   const toast = useToast()
 *   toast.exito('Alerta eliminada')
 *   toast.error(e)                                  // ApiError: usa su mensaje (ya es claro, ver api.js)
 *   toast.error(e, 'No se pudo eliminar la alerta') // texto si el error no trae mensaje
 *   toast.info('...')
 *
 * Los errores de un formulario se siguen mostrando dentro del formulario (Aviso / error del campo).
 */
export function useToast() {
  const contexto = useContext(ToastContext)
  if (!contexto) {
    throw new Error('useToast debe usarse dentro de <ToastProvider>')
  }
  return contexto
}
