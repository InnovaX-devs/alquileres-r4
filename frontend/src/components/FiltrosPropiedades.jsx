import { useState } from 'react'
import { FILTROS_VACIOS, hayFiltrosActivos, validarFiltros } from '../utils/filtros'

const claseCampo =
  'mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200'

function Campo({ id, label, children }) {
  return (
    <div>
      <label htmlFor={id} className="block text-xs font-medium text-gray-600">
        {label}
      </label>
      {children}
    </div>
  )
}

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
    <form
      onSubmit={enviar}
      noValidate
      role="search"
      aria-label="Filtrar propiedades"
      className="mb-6 rounded-lg border border-gray-200 bg-white p-4 shadow-sm"
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <Campo id="filtro-calle" label="Calle">
          <input
            id="filtro-calle"
            type="search"
            placeholder="Ej: Sobremonte"
            value={borrador.calle}
            onChange={cambiar('calle')}
            className={claseCampo}
          />
        </Campo>
        <Campo id="filtro-moneda" label="Moneda">
          <select id="filtro-moneda" value={borrador.moneda} onChange={cambiar('moneda')} className={claseCampo}>
            <option value="">Todas</option>
            <option value="ARS">Pesos ($)</option>
            <option value="USD">Dólares (US$)</option>
          </select>
        </Campo>
        <Campo id="filtro-precio-min" label="Precio mínimo">
          <input
            id="filtro-precio-min"
            type="number"
            inputMode="numeric"
            min="0"
            placeholder="Desde"
            value={borrador.precioMin}
            onChange={cambiar('precioMin')}
            className={claseCampo}
          />
        </Campo>
        <Campo id="filtro-precio-max" label="Precio máximo">
          <input
            id="filtro-precio-max"
            type="number"
            inputMode="numeric"
            min="0"
            placeholder="Hasta"
            value={borrador.precioMax}
            onChange={cambiar('precioMax')}
            className={claseCampo}
          />
        </Campo>
        <Campo id="filtro-habitaciones" label="Habitaciones">
          <select
            id="filtro-habitaciones"
            value={borrador.habitacionesMin}
            onChange={cambiar('habitacionesMin')}
            className={claseCampo}
          >
            <option value="">Cualquier cantidad</option>
            <option value="1">1 o más</option>
            <option value="2">2 o más</option>
            <option value="3">3 o más</option>
            <option value="4">4 o más</option>
          </select>
        </Campo>
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
          <button
            type="button"
            onClick={limpiar}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Limpiar filtros
          </button>
        )}
        <button type="submit" className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
          Buscar
        </button>
      </div>
    </form>
  )
}
