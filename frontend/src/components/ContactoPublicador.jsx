import { useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import BotonFavorito from './BotonFavorito'
import { Button, Card, Spinner } from './ui'
import { datosFaltantesParaVerificar, unirConY } from '../utils/perfil'

function Caja({ children }) {
  return (
    <Card as="section" aria-labelledby="titulo-contacto">
      <h2 id="titulo-contacto" className="text-xl font-bold text-gray-900">
        Contacto del publicador
      </h2>
      <div className="mt-3">{children}</div>
    </Card>
  )
}

function Aviso({ texto, accion, to, state }) {
  return (
    <>
      <p className="text-[15px] text-gray-600">{texto}</p>
      <Button variante="secundario" to={to} state={state} anchoCompleto className="mt-4">
        {accion}
      </Button>
    </>
  )
}

function Dato({ icono, children }) {
  return (
    <li className="flex items-center gap-2 text-sm text-gray-700">
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-4 w-4 shrink-0 text-gray-400">
        <path strokeLinecap="round" strokeLinejoin="round" d={icono} />
      </svg>
      {children}
    </li>
  )
}

const ICONO_EMAIL = 'M4 6h16v12H4zM4 7l8 6 8-6'
const ICONO_TELEFONO =
  'M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2Z'

/**
 * Sección de contacto del detalle (#75). El backend solo manda `contacto` a usuarios
 * con sesión y perfil verificado; si llega null, se explica qué le falta al usuario.
 *
 * Si publica una inmobiliaria, se la puede guardar en favoritos (#83).
 *
 * @param {object|null} props.contacto  { nombre, email, telefono, tipo: 'USUARIO' | 'INMOBILIARIA', inmobiliariaId }
 */
export default function ContactoPublicador({ contacto }) {
  const { estaAutenticado, usuario, cargando } = useAuth()
  const location = useLocation()

  if (contacto) {
    return (
      <Caja>
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="font-bold text-gray-900">{contacto.nombre}</p>
            <p className="text-xs text-gray-500">
              {contacto.tipo === 'INMOBILIARIA' ? 'Inmobiliaria' : 'Particular'}
            </p>
          </div>
          {contacto.tipo === 'INMOBILIARIA' && contacto.inmobiliariaId && (
            <BotonFavorito tipo="inmobiliaria" id={contacto.inmobiliariaId} className="border-2 border-gray-200 shadow-none" />
          )}
        </div>
        <ul className="mt-3 space-y-2">
          {contacto.email && (
            <Dato icono={ICONO_EMAIL}>
              <a href={`mailto:${contacto.email}`} className="font-bold text-rio-700 hover:underline break-all">
                {contacto.email}
              </a>
            </Dato>
          )}
          {contacto.telefono && (
            <Dato icono={ICONO_TELEFONO}>
              <a href={`tel:${contacto.telefono.replace(/[^\d+]/g, '')}`} className="font-bold text-rio-700 hover:underline">
                {contacto.telefono}
              </a>
            </Dato>
          )}
        </ul>
      </Caja>
    )
  }

  if (!estaAutenticado) {
    return (
      <Caja>
        <Aviso
          texto="Iniciá sesión para ver el nombre, email y teléfono de quien publicó esta propiedad."
          accion="Iniciar sesión"
          to="/login"
          state={{ from: location }}
        />
      </Caja>
    )
  }

  // Hay sesión pero todavía no llegó el usuario (justo después de recargar)
  if (cargando || !usuario) {
    return (
      <Caja>
        <Spinner etiqueta="Cargando" className="text-rio-600" />
      </Caja>
    )
  }

  if (!usuario.verificado) {
    const faltantes = datosFaltantesParaVerificar(usuario)
    return (
      <Caja>
        <Aviso
          texto={
            faltantes.length > 0
              ? `Para ver el contacto tenés que verificar tu perfil. Te falta completar: ${unirConY(faltantes)}.`
              : 'Para ver el contacto tenés que verificar tu perfil.'
          }
          accion="Completar mi perfil"
          to="/perfil"
        />
      </Caja>
    )
  }

  // Verificado, pero la propiedad no tiene publicador asociado
  return (
    <Caja>
      <p className="text-sm text-gray-600">El contacto de esta publicación no está disponible.</p>
    </Caja>
  )
}
