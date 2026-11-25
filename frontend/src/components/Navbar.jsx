import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useVisitasPendientes } from '../hooks/useVisitasPendientes'

const linkEscritorio = ({ isActive }) =>
  `px-3 py-2 rounded-md text-sm font-medium whitespace-nowrap ${
    isActive ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:bg-gray-100'
  }`

const linkMovil = ({ isActive }) =>
  `block px-3 py-3 rounded-md text-base font-medium ${
    isActive ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:bg-gray-100'
  }`

function Contador({ cantidad }) {
  if (cantidad <= 0) return null
  return (
    <span
      aria-hidden="true"
      className="min-w-5 rounded-full bg-red-600 px-1.5 text-center text-xs font-semibold leading-5 text-white"
    >
      {cantidad > 9 ? '9+' : cantidad}
    </span>
  )
}

/** Links de la barra. Se usan dos veces: en fila (escritorio) y en columna (menú del celular). */
function Links({ claseLink, claseSalir, estaAutenticado, usuario, pendientes, onSalir }) {
  return (
    <>
      <NavLink to="/" end className={claseLink}>Propiedades</NavLink>
      {estaAutenticado ? (
        <>
          <NavLink to="/publicar" className={claseLink}>Publicar</NavLink>
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
    <header className="border-b border-gray-200 bg-white">
      <nav aria-label="Principal" className="mx-auto max-w-6xl px-4">
        <div className="flex h-16 items-center justify-between gap-4">
          <Link to="/" className="shrink-0 text-xl font-bold text-blue-600">
            Alquileres R4
          </Link>

          <div className="hidden items-center gap-1 xl:flex">
            <Links
              claseLink={linkEscritorio}
              claseSalir="px-3 py-2 rounded-md text-sm font-medium whitespace-nowrap text-gray-700 hover:bg-gray-100"
              {...propsLinks}
            />
          </div>

          <button
            type="button"
            onClick={() => setAbierto((valor) => !valor)}
            aria-expanded={abierto}
            aria-controls="menu-movil"
            aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'}
            className="relative rounded-md p-2 text-gray-700 hover:bg-gray-100 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 xl:hidden"
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
              <span aria-hidden="true" className="absolute right-1 top-1 h-2.5 w-2.5 rounded-full bg-red-600 ring-2 ring-white" />
            )}
          </button>
        </div>

        {abierto && (
          <div id="menu-movil" className="flex flex-col gap-1 border-t border-gray-100 py-3 xl:hidden">
            <Links
              claseLink={linkMovil}
              claseSalir="block w-full px-3 py-3 rounded-md text-left text-base font-medium text-gray-700 hover:bg-gray-100"
              {...propsLinks}
            />
          </div>
        )}
      </nav>
    </header>
  )
}
