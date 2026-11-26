import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useVisitasPendientes } from '../hooks/useVisitasPendientes'
import logoAlkilea from '../assets/alkilea-wordmark-petroleo.svg'

// Barra clara sobre el fondo arena: links en gris, el activo en negrita petróleo y Publicar en un botón petróleo
const foco = 'focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600'

const linkEscritorio = ({ isActive }) =>
  `px-2.5 py-2 rounded-lg text-[15px] whitespace-nowrap transition-colors ${foco} ${
    isActive ? 'text-gray-900 font-bold' : 'text-gray-600 hover:text-gray-900'
  }`

const linkMovil = ({ isActive }) =>
  `block px-3 py-3 rounded-lg text-base ${foco} ${
    isActive ? 'text-gray-900 font-bold' : 'text-gray-600 hover:bg-gray-200/60 hover:text-gray-900'
  }`

const publicarEscritorio = ({ isActive }) =>
  `ml-2 px-5 py-2.5 rounded-[14px] text-[15px] font-bold whitespace-nowrap transition-colors ${foco} ${
    isActive ? 'bg-pino-700 text-gray-100' : 'bg-pino-800 text-gray-100 hover:bg-pino-700'
  }`

const publicarMovil = () => `mt-1 block px-5 py-3 rounded-[14px] text-base font-bold bg-pino-800 text-gray-100 ${foco}`

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
function Links({ claseLink, clasePublicar, claseSalir, estaAutenticado, usuario, pendientes, onSalir, enAcceso }) {
  return (
    <>
      {enAcceso ? (
        // En ingreso y registro: una vuelta clara al listado en lugar del link "Propiedades"
        <NavLink to="/" end className={claseLink}>
          <span className="inline-flex items-center gap-1.5">
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4">
              <path d="m15 6-6 6 6 6" />
            </svg>
            Continuar viendo propiedades
          </span>
        </NavLink>
      ) : (
        <NavLink to="/" end className={claseLink}>Propiedades</NavLink>
      )}
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
          {/* Sin sesión, un solo botón (mismo estilo que Publicar): el registro se ofrece en la pantalla de ingreso */}
          {!enAcceso && <NavLink to="/login" className={clasePublicar}>Iniciar sesión</NavLink>}
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

  // En las pantallas de ingreso y registro no se ofrece "Iniciar sesión" (ya se está ahí)
  const enAcceso = ['/login', '/registro'].includes(location.pathname)
  const propsLinks = { estaAutenticado, usuario, pendientes, onSalir: cerrarSesion, enAcceso }

  return (
    <header>
      <nav aria-label="Principal" className="mx-auto max-w-6xl px-4">
        <div className="flex h-[84px] items-center justify-between gap-4">
          {/* Marca: el logotipo de Alkilea (el alt es el nombre del link) */}
          <Link to="/" className={`flex shrink-0 items-center rounded-lg ${foco}`}>
            <img src={logoAlkilea} alt="Alkilea" className="h-8 w-auto" />
          </Link>

          <div className="hidden items-center gap-2 xl:flex">
            <Links
              claseLink={linkEscritorio}
              clasePublicar={publicarEscritorio}
              claseSalir={`px-2.5 py-2 rounded-lg text-[15px] whitespace-nowrap text-gray-600 hover:text-gray-900 ${foco}`}
              {...propsLinks}
            />
          </div>

          <button
            type="button"
            onClick={() => setAbierto((valor) => !valor)}
            aria-expanded={abierto}
            aria-controls="menu-movil"
            aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'}
            className={`relative flex h-11 w-11 items-center justify-center rounded-lg text-gray-900 hover:bg-gray-200/60 xl:hidden ${foco}`}
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
              <span aria-hidden="true" className="absolute right-1.5 top-1.5 h-2.5 w-2.5 rounded-full bg-sol-400 ring-2 ring-gray-100" />
            )}
          </button>
        </div>

        {abierto && (
          <div id="menu-movil" className="flex animate-aparecer flex-col gap-1 border-t border-gray-300 py-3 xl:hidden">
            <Links
              claseLink={linkMovil}
              clasePublicar={publicarMovil}
              claseSalir={`block w-full px-3 py-3 rounded-lg text-left text-base text-gray-600 hover:bg-gray-200/60 hover:text-gray-900 ${foco}`}
              {...propsLinks}
            />
          </div>
        )}
      </nav>
    </header>
  )
}
