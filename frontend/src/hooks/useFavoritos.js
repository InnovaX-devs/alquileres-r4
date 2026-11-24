import { useContext } from 'react'
import { FavoritosContext } from '../context/favoritosContextValue'

/**
 * const { esFavorito, alternar } = useFavoritos()
 * esFavorito('propiedad' | 'inmobiliaria', id) -> true | false | undefined (todavía cargando)
 * alternar('propiedad' | 'inmobiliaria', id)   -> Promise; si falla lanza un ApiError
 */
export function useFavoritos() {
  const contexto = useContext(FavoritosContext)
  if (!contexto) {
    throw new Error('useFavoritos debe usarse dentro de <FavoritosProvider>')
  }
  return contexto
}
