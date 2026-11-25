import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useVisitasPendientes } from '../hooks/useVisitasPendientes'

// Barra verde pino: links claros, el activo con fondo y el de Publicar en amarillo (acción principal)
const foco = 'focus:outline-none focus-visible:ring-3 focus-visible:ring-sol-400'

const linkEscritorio = ({ isActive }) =>
  `px-3 py-2 rounded-lg text-[15px] whitespace-nowrap transition-colors ${foco} ${
    isActive ? 'bg-white/12 text-white font-bold' : 'text-gray-300 hover:bg-white/8 hover:text-white'
  }`

const linkMovil = ({ isActive }) =>
  `block px-3 py-3 rounded-lg text-base ${foco} ${
    isActive ? 'bg-white/12 text-white font-bold' : 'text-gray-200 hover:bg-white/8'
  }`

const publicarEscritorio = ({ isActive }) =>
  `ml-1 px-4 py-2 rounded-xl text-[15px] font-bold whitespace-nowrap transition-colors ${foco} ${
    isActive ? 'bg-sol-300 text-pino-800' : 'bg-sol-400 text-pino-800 hover:bg-sol-300'
  }`

const publicarMovil = () => `block px-3 py-3 rounded-xl text-base font-bold bg-sol-400 text-pino-800 ${foco}`

/** Marca: el techo (amarillo) sobre el río (verde agua). */
export function Logo({ className = 'h-8 w-8' }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 32 32" fill="none" className={className}>
      <path d="M3 23c4-3 7-3 10 0s6 3 10 0 5-2 6-1" stroke="#7fc4b6" strokeWidth="2.4" strokeLinecap="round" />
      <path d="M6 16 16 7l10 9" stroke="#f2b33d" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function Contador({ cantidad }) {
  if (cantidad <= 0) return null
  return (
    <span
      aria-hidden="true"
      className="min-w-5 rounded-full bg-sol-400 px-1.5 text-center text-xs font-bold leading-5 text-pino-800"
    >
      {cantidad > 9 ? '9+' : cantidad}
    </span>
  )
}

/** Links de la barra. Se usan dos veces: en fila (escritorio) y en columna (menú del celular). */
function Links({ claseLink, clasePublicar, claseSalir, estaAutenticado, usuario, pendientes, onSalir }) {
  return (
    <>
      <NavLink to="/" end className={claseLink}>Propiedades</NavLink>
      {estaAutenticado ? (
        <>
          <NavLink to="/mis-propiedades" className={claseLink}>Mis propiedades</NavLink>
          <NavLink to="/favoritos" className={claseLink}>Favoritos</NavLink>
          <NavLink to="/alertas" className={claseLink}>Alertas</NavLink>
          <NavLink
            to="/visitas"
            className={claseLink}
            aria-label={
              pendientes > 0 ? `Visitas, ${pendientes} ${pendientes === 1 ? 'pendiente' : 'pendientes'}` : undefined
            }
          >
            <span className="inline-flex items-center gap-1.5">
              Visitas
              <Contador cantidad={pendientes} />
            </span>
          </NavLink>
          <NavLink to="/perfil" className={claseLink}>
            <span className="block max-w-[12rem] truncate">{usuario?.nombre || 'Mi perfil'}</span>
          </NavLink>
          <button
            type="button"
            onClick={onSalir}
            className={claseSalir}
          >
            Salir
          </button>
          <NavLink to="/publicar" className={clasePublicar}>Publicar</NavLink>
        </>
      ) : (
        <>
          <NavLink to="/login" className={claseLink}>Ingresar</NavLink>
          <NavLink to="/registro" className={claseLink}>Registrarse</NavLink>
        </>
      )}
    </>
  )
}

// Barra de navegación (#89): desde 1280 px los links van en fila;
// en celulares, tablets y notebooks chicas se agrupan en un menú desplegable.
export default function Navbar() {
  const navigate = useNavigate()
  const location = useLocation()
  const { estaAutenticado, usuario, logout } = useAuth()
  const pendientes = useVisitasPendientes()
  const [abierto, setAbierto] = useState(false)

  // El menú se cierra al cambiar de página (también con el botón atrás del navegador)
  const [rutaAnterior, setRutaAnterior] = useState(location.pathname)
  if (rutaAnterior !== location.pathname) {
    setRutaAnterior(location.pathname)
    setAbierto(false)
  }

  // Y con Escape
  useEffect(() => {
    if (!abierto) return
    const alPresionar = (event) => {
      if (event.key === 'Escape') setAbierto(false)
    }
    document.addEventListener('keydown', alPresionar)
    return () => document.removeEventListener('keydown', alPresionar)
  }, [abierto])

  const cerrarSesion = () => {
    setAbierto(false)
    logout()
    navigate('/login')
  }

  const propsLinks = { estaAutenticado, usuario, pendientes, onSalir: cerrarSesion }

  return (
    <header className="bg-pino-800">
      <nav aria-label="Principal" className="mx-auto max-w-6xl px-4">
        <div className="flex h-16 items-center justify-between gap-4">
          <Link to="/" className={`flex shrink-0 items-center gap-2.5 rounded-lg text-white ${foco}`}>
            <Logo />
            <span className="font-display text-[21px] font-extrabold tracking-tight">Alquileres R4</span>
          </Link>

          <div className="hidden items-center gap-1 xl:flex">
            <Links
              claseLink={linkEscritorio}
              clasePublicar={publicarEscritorio}
              claseSalir={`px-3 py-2 rounded-lg text-[15px] whitespace-nowrap text-gray-300 hover:bg-white/8 hover:text-white ${foco}`}
              {...propsLinks}
            />
          </div>

          <button
            type="button"
            onClick={() => setAbierto((valor) => !valor)}
            aria-expanded={abierto}
            aria-controls="menu-movil"
            aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'}
            className={`relative flex h-11 w-11 items-center justify-center rounded-lg text-white hover:bg-white/10 xl:hidden ${foco}`}
          >
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-6 w-6">
              {abierto ? (
                <path strokeLinecap="round" d="M6 6l12 12M18 6 6 18" />
              ) : (
                <path strokeLinecap="round" d="M4 7h16M4 12h16M4 17h16" />
              )}
            </svg>
            {/* Aviso de visitas pendientes también con el menú cerrado */}
            {!abierto && pendientes > 0 && (
              <span aria-hidden="true" className="absolute right-1.5 top-1.5 h-2.5 w-2.5 rounded-full bg-sol-400 ring-2 ring-pino-800" />
            )}
          </button>
        </div>

        {abierto && (
          <div id="menu-movil" className="flex animate-aparecer flex-col gap-1 border-t border-white/10 py-3 xl:hidden">
            <Links
              claseLink={linkMovil}
              clasePublicar={publicarMovil}
              claseSalir={`block w-full px-3 py-3 rounded-lg text-left text-base text-gray-200 hover:bg-white/8 ${foco}`}
              {...propsLinks}
            />
          </div>
        )}
      </nav>
    </header>
  )
}
