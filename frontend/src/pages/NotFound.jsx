import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="text-center py-16">
      <p className="text-6xl font-bold text-blue-600">404</p>
      <h1 className="mt-4 text-2xl font-semibold">Página no encontrada</h1>
      <p className="mt-2 text-gray-500">La página que buscás no existe o fue movida.</p>
      <Link
        to="/"
        className="inline-block mt-6 px-4 py-2 rounded-md bg-blue-600 text-white hover:bg-blue-700"
      >
        Volver al inicio
      </Link>
    </div>
  )
}