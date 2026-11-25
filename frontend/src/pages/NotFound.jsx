import { Button } from '../components/ui'

export default function NotFound() {
  return (
    <div className="text-center py-16">
      <p className="text-6xl font-bold text-blue-600">404</p>
      <h1 className="mt-4 text-2xl font-semibold">Página no encontrada</h1>
      <p className="mt-2 text-gray-500">La página que buscás no existe o fue movida.</p>
      <Button to="/" className="mt-6">
        Volver al inicio
      </Button>
    </div>
  )
}