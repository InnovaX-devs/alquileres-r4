import { useState } from 'react'
import { FILTROS_VACIOS, hayFiltrosActivos, validarFiltros } from '../utils/filtros'
import { Button, Card, Input, Select } from './ui'

/**
 * Filtros del listado (#73). Se aplican al tocar "Buscar" (no en cada tecla) y se pueden limpiar.
 *
 * @param {object}   props.valores   filtros aplicados (los de la URL)
 * @param {Function} props.onAplicar recibe el objeto de filtros nuevo
 */
export default function FiltrosPropiedades({ valores, onAplicar }) {
  const [borrador, setBorrador] = useState(valores)
  const [error, setError] = useState(null)
  // Si los filtros aplicados cambian desde afuera (botón atrás, link), el formulario los refleja
  const [aplicadosPrevios, setAplicadosPrevios] = useState(valores)
  if (aplicadosPrevios !== valores) {
    setAplicadosPrevios(valores)
    setBorrador(valores)
    setError(null)
  }

  const cambiar = (clave) => (event) => {
    setBorrador((anterior) => ({ ...anterior, [clave]: event.target.value }))
    setError(null)
  }

  const enviar = (event) => {
    event.preventDefault()
    const encontrado = validarFiltros(borrador)
    if (encontrado) {
      setError(encontrado)
      return
    }
    onAplicar({ ...borrador, calle: borrador.calle.trim() })
  }

  const limpiar = () => {
    setBorrador(FILTROS_VACIOS)
    setError(null)
    onAplicar(FILTROS_VACIOS)
  }

  const precioSinMoneda = (borrador.precioMin !== '' || borrador.precioMax !== '') && borrador.moneda === ''

  return (
    <Card
      as="form"
      padding="sm"
      onSubmit={enviar}
      noValidate
      role="search"
      aria-label="Filtrar propiedades"
      className="mb-6"
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <Input
          id="filtro-calle"
          label="Calle"
          type="search"
          placeholder="Ej: Sobremonte"
          value={borrador.calle}
          onChange={cambiar('calle')}
        />
        <Select id="filtro-moneda" label="Moneda" value={borrador.moneda} onChange={cambiar('moneda')}>
          <option value="">Todas</option>
          <option value="ARS">Pesos ($)</option>
          <option value="USD">Dólares (US$)</option>
        </Select>
        <Input
          id="filtro-precio-min"
          label="Precio mínimo"
          type="number"
          inputMode="numeric"
          min="0"
          placeholder="Desde"
          value={borrador.precioMin}
          onChange={cambiar('precioMin')}
        />
        <Input
          id="filtro-precio-max"
          label="Precio máximo"
          type="number"
          inputMode="numeric"
          min="0"
          placeholder="Hasta"
          value={borrador.precioMax}
          onChange={cambiar('precioMax')}
        />
        <Select
          id="filtro-habitaciones"
          label="Habitaciones"
          value={borrador.habitacionesMin}
          onChange={cambiar('habitacionesMin')}
        >
          <option value="">Cualquier cantidad</option>
          <option value="1">1 o más</option>
          <option value="2">2 o más</option>
          <option value="3">3 o más</option>
          <option value="4">4 o más</option>
        </Select>
      </div>

      {error && (
        <p role="alert" className="mt-3 text-sm text-red-600">
          {error}
        </p>
      )}
      {!error && precioSinMoneda && (
        <p className="mt-3 text-xs text-gray-500">
          Tip: elegí una moneda para no mezclar precios en pesos y en dólares.
        </p>
      )}

      <div className="mt-4 flex flex-wrap justify-end gap-2">
        {(hayFiltrosActivos(borrador) || hayFiltrosActivos(valores)) && (
          <Button variante="secundario" onClick={limpiar}>
            Limpiar filtros
          </Button>
        )}
        <Button type="submit">Buscar</Button>
      </div>
    </Card>
  )
}
