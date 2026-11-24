import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

const linkClass = ({ isActive }) =>
  `px-3 py-2 rounded-md text-sm font-medium ${
    isActive ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:bg-gray-100'
  }`

export default function Navbar() {
  const navigate = useNavigate()
  const { estaAutenticado, usuario, logout } = useAuth()

  const cerrarSesion = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="bg-white border-b border-gray-200">
      <nav className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/" className="text-xl font-bold text-blue-600">
          Alquileres R4
        </Link>

        <div className="flex items-center gap-1">
          <NavLink to="/" end className={linkClass}>Propiedades</NavLink>

          {estaAutenticado ? (
            <>
              <NavLink to="/publicar" className={linkClass}>Publicar</NavLink>
              <NavLink to="/mis-propiedades" className={linkClass}>Mis propiedades</NavLink>
              <NavLink to="/visitas" className={linkClass}>Visitas</NavLink>
              <NavLink to="/perfil" className={linkClass}>
                {usuario?.nombre || 'Mi perfil'}
              </NavLink>
              <button
                onClick={cerrarSesion}
                className="px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-100"
              >
                Salir
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={linkClass}>Ingresar</NavLink>
              <NavLink to="/registro" className={linkClass}>Registrarse</NavLink>
            </>
          )}
        </div>
      </nav>
    </header>
  )
}