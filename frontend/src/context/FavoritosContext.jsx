import { useCallback, useEffect, useMemo, useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { FavoritosContext } from './favoritosContextValue'
import {
  guardarInmobiliariaFavorita,
  guardarPropiedadFavorita,
  listarFavoritos,
  quitarInmobiliariaFavorita,
  quitarPropiedadFavorita,
} from '../services/favoritos'

const ACCIONES = {
  propiedad: { guardar: guardarPropiedadFavorita, quitar: quitarPropiedadFavorita },
  inmobiliaria: { guardar: guardarInmobiliariaFavorita, quitar: quitarInmobiliariaFavorita },
}

const VACIO = { propiedad: new Set(), inmobiliaria: new Set() }

/**
 * Favoritos del usuario con sesión (#82 y #83).
 *
 * Se piden a GET /favoritos una vez por sesión, así el corazón aparece marcado al recargar
 * la página. Al marcar o desmarcar se actualiza al instante y, si el backend falla, se revierte.
 */
export function FavoritosProvider({ children }) {
  const { token } = useAuth()
  // ids guardados, con el token al que corresponden (al cambiar de sesión se descartan)
  const [favoritos, setFavoritos] = useState({ token: null, ...VACIO })

  useEffect(() => {
    if (!token) return
    let cancelado = false
    listarFavoritos()
      .then((datos) => {
        if (cancelado) return
        setFavoritos({
          token,
          propiedad: new Set((datos?.propiedades ?? []).map((f) => f.propiedadId)),
          inmobiliaria: new Set((datos?.inmobiliarias ?? []).map((f) => f.inmobiliariaId)),
        })
      })
      .catch(() => {
        // Si falla se muestran sin marcar; al tocar el corazón el backend corrige con 409
        if (!cancelado) setFavoritos({ token, ...VACIO })
      })
    return () => {
      cancelado = true
    }
  }, [token])

  const cargados = !!token && favoritos.token === token

  const esFavorito = useCallback(
    (tipo, id) => (cargados ? favoritos[tipo].has(id) : undefined),
    [cargados, favoritos],
  )

  const cambiar = useCallback((tipo, id, marcado) => {
    setFavoritos((previo) => {
      const ids = new Set(previo[tipo])
      if (marcado) ids.add(id)
      else ids.delete(id)
      return { ...previo, [tipo]: ids }
    })
  }, [])

  const alternar = useCallback(
    async (tipo, id) => {
      const marcar = !favoritos[tipo].has(id)
      cambiar(tipo, id, marcar)
      try {
        if (marcar) await ACCIONES[tipo].guardar(id)
        else await ACCIONES[tipo].quitar(id)
      } catch (e) {
        // 409 al guardar (ya estaba) o 404 al quitar (ya no estaba): el estado final es el pedido
        const yaEstaba = (marcar && e.status === 409) || (!marcar && e.status === 404)
        if (!yaEstaba) {
          cambiar(tipo, id, !marcar)
          throw e
        }
      }
    },
    [favoritos, cambiar],
  )

  const valor = useMemo(() => ({ esFavorito, alternar }), [esFavorito, alternar])

  return <FavoritosContext.Provider value={valor}>{children}</FavoritosContext.Provider>
}
