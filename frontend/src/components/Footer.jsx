import { Link } from 'react-router-dom'
import { Logo } from './Navbar'

const ANIO = new Date().getFullYear()

export default function Footer() {
  return (
    <footer className="mt-8 bg-pino-800 text-gray-300">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4 px-4 py-8">
        <Link to="/" className="flex items-center gap-2 rounded-lg text-white focus:outline-none focus-visible:ring-3 focus-visible:ring-sol-400">
          <Logo className="h-7 w-7" />
          <span className="font-display text-lg font-extrabold tracking-tight">Alquileres R4</span>
        </Link>
        <div className="text-sm">
          <p>Alquileres de inmobiliarias y particulares de Río Cuarto.</p>
          <p className="mt-1 text-gray-400">© {ANIO} Alquileres R4</p>
        </div>
      </div>
    </footer>
  )
}
