import { createContext } from 'react'

// Separado de FavoritosContext.jsx para que Fast Refresh funcione (ese archivo solo exporta componentes)
export const FavoritosContext = createContext(null)
