import { useEffect, useState } from 'react'
import { useAuth } from './useAuth'
import { listarVisitasRecibidas, onVisitasCambiadas } from '../services/visitas'

/**
 * Cantidad de solicitudes de visita recibidas que siguen pendientes (contador de la navbar).
 * Se vuelve a consultar al aceptar o rechazar una solicitud y al volver a la pestaña del navegador.
 * Sin sesión devuelve 0 y no consulta nada.
 */
export function useVisitasPendientes() {
  const { token } = useAuth()
  const [conteo, setConteo] = useState({ token: null, pendientes: 0 })

  useEffect(() => {
    if (!token) return
    let vigente = true

    const consultar = () => {
      listarVisitasRecibidas()
        .then((recibidas) => {
          if (!vigente) return
          const pendientes = (recibidas ?? []).filter((s) => s.estado === 'PENDIENTE').length
          setConteo({ token, pendientes })
        })
        .catch(() => {
          // Si falla, se deja el último valor: el contador es solo un aviso
        })
    }

    const alVolverALaPestana = () => {
      if (document.visibilityState === 'visible') consultar()
    }

    consultar()
    const desuscribir = onVisitasCambiadas(consultar)
    document.addEventListener('visibilitychange', alVolverALaPestana)
    return () => {
      vigente = false
      desuscribir()
      document.removeEventListener('visibilitychange', alVolverALaPestana)
    }
  }, [token])

  // Al cerrar sesión (o cambiar de usuario) no se muestra el número del usuario anterior
  return token && conteo.token === token ? conteo.pendientes : 0
}
