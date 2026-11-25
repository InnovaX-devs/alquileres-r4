import { useState } from 'react'
import { FILTROS_VACIOS, hayFiltrosActivos, validarFiltros } from '../utils/filtros'
import { Button, Input } from './ui'

const MONEDAS = [
  { valor: '', label: 'Todas' },
  { valor: 'ARS', label: 'Pesos' },
  { valor: 'USD', label: 'Dólares' },
]

const HABITACIONES = [
  { valor: '', label: 'Todas' },
  { valor: '1', label: '1+' },
  { valor: '2', label: '2+' },
  { valor: '3', label: '3+' },
  { valor: '4', label: '4+' },
]

const foco = 'focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600 focus-visible:ring-offset-1'

/** Grupo de botones donde se elige una opción (moneda, habitaciones). */
function Opciones({ etiqueta, id, opciones, valor, onElegir }) {
  return (
    <div>
      <p id={id} className="text-sm font-bold text-gray-700">
        {etiqueta}
      </p>
      <div role="group" aria-labelledby={id} className="mt-1.5 flex flex-wrap gap-1.5">
        {opciones.map((opcion) => {
          const activa = valor === opcion.valor
          return (
            <button
              key={opcion.valor || 'todas'}
              type="button"
              onClick={() => onElegir(opcion.valor)}
              aria-pressed={activa}
              className={`min-h-11 min-w-11 rounded-full border-2 px-3.5 text-[15px] font-bold transition-colors ${foco} ${
                activa
                  ? 'border-pino-800 bg-pino-800 text-white'
                  : 'border-gray-200 bg-white text-gray-800 hover:border-gray-300'
              }`}
            >
              {opcion.label}
            </button>
          )
        })}
      </div>
    </div>
  )
}

/**
 * Buscador del inicio (#73, rediseño): calle, precio, moneda y habitaciones. Se aplica al tocar
 * "Buscar" (no en cada tecla) y se puede limpiar. Moneda y habitaciones se eligen con un toque.
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

  const asignar = (clave, valor) => {
    setBorrador((anterior) => ({ ...anterior, [clave]: valor }))
    setError(null)
  }
  const cambiar = (clave) => (event) => asignar(clave, event.target.value)

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
      aria-label="Buscar propiedades"
      className="rounded-3xl bg-white p-5 shadow-xl shadow-pino-900/15 sm:p-6"
    >
      <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-[1.3fr_1.2fr_1fr]">
        <Input
          id="filtro-calle"
          label="Calle"
          type="search"
          placeholder="Ej: Sobremonte, Constitución"
          value={borrador.calle}
          onChange={cambiar('calle')}
        />
        <div>
          <p id="filtro-precio" className="text-sm font-bold text-gray-700">
            Precio por mes
          </p>
          <div role="group" aria-labelledby="filtro-precio" className="grid grid-cols-2 gap-2">
            <Input
              id="filtro-precio-min"
              aria-label="Precio desde"
              type="number"
              inputMode="numeric"
              min="0"
              placeholder="Desde"
              value={borrador.precioMin}
              onChange={cambiar('precioMin')}
            />
            <Input
              id="filtro-precio-max"
              aria-label="Precio hasta"
              type="number"
              inputMode="numeric"
              min="0"
              placeholder="Hasta"
              value={borrador.precioMax}
              onChange={cambiar('precioMax')}
            />
          </div>
        </div>
        <Opciones
          etiqueta="Moneda"
          id="filtro-moneda"
          opciones={MONEDAS}
          valor={borrador.moneda}
          onElegir={(valor) => asignar('moneda', valor)}
        />
      </div>

      <div className="mt-5 flex flex-wrap items-end justify-between gap-4">
        <Opciones
          etiqueta="Habitaciones"
          id="filtro-habitaciones"
          opciones={HABITACIONES}
          valor={borrador.habitacionesMin}
          onElegir={(valor) => asignar('habitacionesMin', valor)}
        />
        <div className="flex gap-2 max-sm:w-full">
          {(hayFiltrosActivos(borrador) || hayFiltrosActivos(valores)) && (
            <Button variante="fantasma" tamanio="lg" onClick={limpiar}>
              Limpiar
            </Button>
          )}
          <Button type="submit" tamanio="lg" className="max-sm:flex-1">
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" className="h-5 w-5">
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-3.5-3.5" />
            </svg>
            Buscar
          </Button>
        </div>
      </div>

      {error && (
        <p role="alert" className="mt-4 text-sm font-bold text-red-700">
          {error}
        </p>
      )}
      {!error && precioSinMoneda && (
        <p className="mt-4 text-sm text-gray-600">
          Elegí una moneda para no mezclar precios en pesos y en dólares.
        </p>
      )}
    </form>
  )
}
