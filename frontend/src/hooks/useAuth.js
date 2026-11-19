import { useContext } from 'react'
import { AuthContext } from '../context/authContextValue'

/**
 * const { usuario, token, cargando, estaAutenticado, login, logout, recargarUsuario } = useAuth()
 */
export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return contexto
}
