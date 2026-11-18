import { useParams } from 'react-router-dom'

export default function PropiedadDetalle() {
  const { id } = useParams()
  return <h1 className="text-2xl font-bold">Propiedad #{id}</h1>
}