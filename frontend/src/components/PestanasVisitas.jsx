import { NavLink } from 'react-router-dom'

const clase = ({ isActive }) =>
  `-mb-px border-b-2 px-4 py-2 text-sm font-medium ${
    isActive ? 'border-blue-600 text-blue-700' : 'border-transparent text-gray-600 hover:text-gray-900'
  }`

// Pestañas de la sección Visitas: las que pedí (#80) y las que recibí como publicador (#81)
export default function PestanasVisitas() {
  return (
    <div>
      <h1 className="text-2xl font-bold">Visitas</h1>
      <nav aria-label="Secciones de visitas" className="mt-4 mb-6 flex border-b border-gray-200">
        <NavLink to="/visitas" end className={clase}>
          Mis solicitudes
        </NavLink>
        <NavLink to="/visitas/recibidas" className={clase}>
          Recibidas
        </NavLink>
      </nav>
    </div>
  )
}
