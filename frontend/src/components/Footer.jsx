import { Link } from 'react-router-dom'

const ANIO = new Date().getFullYear()

// Pie coral de una línea: todo el texto en petróleo (el gris no tiene contraste suficiente sobre coral)
export default function Footer() {
  return (
    <footer className="bg-sol-400 text-[15px] text-pino-800">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 gap-y-1.5 px-4 py-6">
        <Link
          to="/"
          className="rounded-lg focus:outline-none focus-visible:ring-3 focus-visible:ring-pino-800"
        >
          {/* El ícono "ak" con techito (el mismo del favicon): su fondo coral se funde con el del footer */}
          <img src="/favicon.svg" alt="Alkilea" className="h-11 w-11" />
        </Link>
        <p>
          Alquileres de inmobiliarias y particulares de Río Cuarto
          <span className="text-pino-800/75"> · © {ANIO}</span>
        </p>
      </div>
    </footer>
  )
}
