import { NavLink } from 'react-router-dom'

const clase = ({ isActive }) =>
  `-mb-[2px] flex min-h-11 items-center border-b-[3px] px-4 text-[15px] font-bold transition-colors focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600 ${
    isActive ? 'border-pino-800 text-gray-900' : 'border-transparent text-gray-500 hover:text-gray-900'
  }`

// Pestañas de la sección Visitas: las que pedí (#80) y las que recibí como publicador (#81)
export default function PestanasVisitas() {
  return (
    <div>
      <h1 className="text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em]">Visitas</h1>
      <nav aria-label="Secciones de visitas" className="mt-4 mb-6 flex border-b-2 border-gray-200">
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
