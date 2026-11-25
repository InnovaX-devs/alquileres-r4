import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useFavoritos } from '../hooks/useFavoritos'
import { useToast } from '../hooks/useToast'

const TEXTOS = {
  propiedad: { guardar: 'Guardar propiedad en favoritos', quitar: 'Quitar propiedad de favoritos' },
  inmobiliaria: { guardar: 'Guardar inmobiliaria en favoritos', quitar: 'Quitar inmobiliaria de favoritos' },
}

/**
 * Corazón para guardar o quitar de favoritos (#82 propiedades, #83 inmobiliarias).
 * Sin sesión lleva al login y vuelve a la página actual.
 *
 * @param {'propiedad'|'inmobiliaria'} props.tipo
 * @param {number} props.id
 * @param {string} [props.className]  posición / tamaño del botón
 */
export default function BotonFavorito({ tipo, id, className = '' }) {
  const { estaAutenticado } = useAuth()
  const { esFavorito, alternar } = useFavoritos()
  const navigate = useNavigate()
  const location = useLocation()
  const [enviando, setEnviando] = useState(false)
  const toast = useToast()

  const marcado = estaAutenticado ? esFavorito(tipo, id) : false
  const etiqueta = marcado ? TEXTOS[tipo].quitar : TEXTOS[tipo].guardar

  const alHacerClick = async () => {
    if (!estaAutenticado) {
      navigate('/login', { state: { from: location } })
      return
    }
    setEnviando(true)
    try {
      await alternar(tipo, id)
      toast.exito(marcado ? 'Se quitó de tus favoritos' : 'Se guardó en tus favoritos')
    } catch (e) {
      toast.error(e, 'No se pudo actualizar el favorito')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <button
      type="button"
      onClick={alHacerClick}
      // undefined = todavía no se cargaron los favoritos de la sesión
      disabled={enviando || marcado === undefined}
      aria-pressed={!!marcado}
      aria-label={etiqueta}
      title={estaAutenticado ? etiqueta : 'Iniciá sesión para guardar favoritos'}
      className={`inline-flex items-center justify-center rounded-full bg-white/90 p-2 shadow-sm transition hover:bg-white focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 disabled:opacity-60 ${className}`}
    >
      <svg
        aria-hidden="true"
        viewBox="0 0 24 24"
        strokeWidth="1.8"
        stroke="currentColor"
        fill={marcado ? 'currentColor' : 'none'}
        className={`h-5 w-5 ${marcado ? 'text-red-500' : 'text-gray-600'}`}
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M12 20.5s-7.5-4.6-9.3-9.3C1.5 8 3.6 4.5 7.1 4.5c2 0 3.6 1.1 4.9 2.9 1.3-1.8 2.9-2.9 4.9-2.9 3.5 0 5.6 3.5 4.4 6.7-1.8 4.7-9.3 9.3-9.3 9.3Z"
        />
      </svg>
    </button>
  )
}
