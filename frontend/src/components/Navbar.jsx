import { Link, NavLink, useNavigate } from 'react-router-dom'

const linkClass = ({ isActive }) =>
  `px-3 py-2 rounded-md text-sm font-medium ${
    isActive ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:bg-gray-100'
  }`

export default function Navbar() {
  const navigate = useNavigate()
  // Provisorio: se reemplaza por AuthContext en el issue #68
  const hayToken = !!localStorage.getItem('token')

  const cerrarSesion = () => {
    localStorage.removeItem('token')
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

          {hayToken ? (
            <>
              <NavLink to="/publicar" className={linkClass}>Publicar</NavLink>
              <NavLink to="/perfil" className={linkClass}>Mi perfil</NavLink>
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