import { createContext } from 'react'

// Separado de ToastContext.jsx para que Fast Refresh funcione (ese archivo solo exporta componentes)
export const ToastContext = createContext(null)
