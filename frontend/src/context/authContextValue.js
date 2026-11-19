import { createContext } from 'react'

// Separado de AuthContext.jsx para que Fast Refresh funcione (ese archivo solo exporta componentes)
export const AuthContext = createContext(null)
