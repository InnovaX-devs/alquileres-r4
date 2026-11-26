import { useEffect, useRef, useState } from 'react'
import { FILTROS_VACIOS, hayFiltrosActivos, validarFiltros } from '../utils/filtros'

const MONEDAS = [
  { valor: '', label: 'Todas' },
  { valor: 'ARS', label: 'Pesos' },
  { valor: 'USD', label: 'Dólares' },
]

const HABITACIONES = [
  { valor: '', label: 'Todas' },
  { valor: '1', label: '+1' },
  { valor: '2', label: '+2' },
  { valor: '3', label: '+3' },
  { valor: '4', label: '+4' },
]

// Campos minimalistas: sin caja, solo una línea inferior que se oscurece al enfocar
const CLASE_CAMPO =
  'block h-[46px] w-full min-w-0 border-0 border-b-[1.5px] border-gray-300 bg-transparent px-0.5 text-[17px] ' +
  'text-gray-900 placeholder:text-gray-400 transition-colors hover:border-gray-400 focus:border-rio-600 focus:outline-none'
const CLASE_ETIQUETA = 'text-sm text-gray-600'

/**
 * Desplegable propio con etiqueta arriba (moneda, ambientes): el <select> nativo abre una lista del
 * sistema que no sigue el diseño. Patrón "select-only combobox" de ARIA: botón + lista de opciones.
 * Teclado: flechas, Inicio/Fin, Enter o Espacio para elegir, Escape o Tab para cerrar.
 */
function Desplegable({ id, etiqueta, opciones, valor, onElegir }) {
  const [abierto, setAbierto] = useState(false)
  const [activo, setActivo] = useState(0)
  const contenedor = useRef(null)
  const boton = useRef(null)
  const lista = useRef(null)

  const indiceElegido = Math.max(0, opciones.findIndex((opcion) => opcion.valor === valor))
  const elegida = opciones[indiceElegido]

  const abrir = () => {
    setActivo(indiceElegido)
    setAbierto(true)
  }
  const cerrar = (devolverFoco = true) => {
    setAbierto(false)
    if (devolverFoco) boton.current?.focus()
  }
  const elegir = (indice) => {
    onElegir(opciones[indice].valor)
    cerrar()
  }

  // Al abrir, el foco pasa a la lista (la opción activa se anuncia con aria-activedescendant)
  useEffect(() => {
    if (abierto) lista.current?.focus()
  }, [abierto])

  // Se cierra al tocar afuera
  useEffect(() => {
    if (!abierto) return
    const alTocar = (event) => {
      if (!contenedor.current?.contains(event.target)) setAbierto(false)
    }
    document.addEventListener('pointerdown', alTocar)
    return () => document.removeEventListener('pointerdown', alTocar)
  }, [abierto])

  const teclaEnBoton = (event) => {
    if (['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(event.key)) {
      event.preventDefault()
      abrir()
    }
  }

  const teclaEnLista = (event) => {
    const ultimo = opciones.length - 1
    const acciones = {
      ArrowDown: () => setActivo((i) => Math.min(ultimo, i + 1)),
      ArrowUp: () => setActivo((i) => Math.max(0, i - 1)),
      Home: () => setActivo(0),
      End: () => setActivo(ultimo),
      Enter: () => elegir(activo),
      ' ': () => elegir(activo),
      Escape: () => cerrar(),
    }
    if (event.key === 'Tab') {
      cerrar(false)
      return
    }
    if (acciones[event.key]) {
      event.preventDefault()
      acciones[event.key]()
    }
  }

  const idEtiqueta = `${id}-etiqueta`
  const idLista = `${id}-lista`

  return (
    <div ref={contenedor} className="relative flex flex-[0_1_150px] flex-col gap-1.5 max-sm:flex-1">
      <span id={idEtiqueta} className={CLASE_ETIQUETA}>
        {etiqueta}
      </span>
      <button
        ref={boton}
        id={id}
        type="button"
        role="combobox"
        aria-haspopup="listbox"
        aria-expanded={abierto}
        aria-controls={idLista}
        aria-labelledby={`${idEtiqueta} ${id}`}
        onClick={() => (abierto ? cerrar() : abrir())}
        onKeyDown={teclaEnBoton}
        className={`flex h-[46px] w-full items-center justify-between gap-2 border-0 border-b-[1.5px] bg-transparent px-0.5 text-left text-[17px] text-gray-900 transition-colors focus:outline-none ${
          abierto ? 'border-rio-600' : 'border-gray-300 hover:border-gray-400 focus-visible:border-rio-600'
        }`}
      >
        <span className="truncate">{elegida.label}</span>
        <svg
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className={`h-[18px] w-[18px] shrink-0 text-gray-600 transition-transform duration-200 ${abierto ? 'rotate-180' : ''}`}
        >
          <path d="m6 9 6 6 6-6" />
        </svg>
      </button>

      {abierto && (
        <ul
          ref={lista}
          id={idLista}
          role="listbox"
          tabIndex={-1}
          aria-labelledby={idEtiqueta}
          aria-activedescendant={`${id}-opcion-${activo}`}
          onKeyDown={teclaEnLista}
          className="absolute left-0 top-full z-30 mt-2 w-max min-w-full max-w-[16rem] animate-aparecer rounded-2xl border border-gray-300 bg-white p-1.5 shadow-[0_18px_40px_-16px_rgba(26,58,62,0.35)] focus:outline-none"
        >
          {opciones.map((opcion, indice) => {
            const seleccionada = indice === indiceElegido
            const resaltada = indice === activo
            return (
              <li
                key={opcion.valor || 'todas'}
                id={`${id}-opcion-${indice}`}
                role="option"
                aria-selected={seleccionada}
                onPointerEnter={() => setActivo(indice)}
                onClick={() => elegir(indice)}
                className={`flex cursor-pointer items-center justify-between gap-4 rounded-xl px-3 py-2.5 text-[15px] text-gray-900 transition-colors ${
                  seleccionada ? `font-bold ${resaltada ? 'bg-sol-200' : 'bg-sol-100'}` : resaltada ? 'bg-gray-100' : ''
                }`}
              >
                <span>{opcion.label}</span>
                <svg
                  aria-hidden="true"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className={`h-4 w-4 shrink-0 text-pino-800 ${seleccionada ? '' : 'invisible'}`}
                >
                  <path d="m5 12 5 5 9-10" />
                </svg>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}

/**
 * Buscador del inicio (#73, rediseño): calle, precio, moneda y ambientes (habitaciones). Se aplica
 * al tocar "Buscar" (no en cada tecla) y se puede limpiar. Va superpuesto al pie del hero.
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

  const hayFiltros = hayFiltrosActivos(borrador) || hayFiltrosActivos(valores)
  const precioSinMoneda = (borrador.precioMin !== '' || borrador.precioMax !== '') && borrador.moneda === ''

  return (
    <form
      onSubmit={enviar}
      noValidate
      role="search"
      aria-label="Buscar propiedades"
      className="relative mx-3 -mt-16 flex flex-wrap items-end gap-x-6 gap-y-5 rounded-[22px] bg-white p-5 shadow-[0_24px_48px_-32px_rgba(26,58,62,0.35)] sm:mx-7"
    >
      <div className="flex flex-[2_1_220px] flex-col gap-1.5">
        <label htmlFor="filtro-calle" className={CLASE_ETIQUETA}>
          Calle
        </label>
        <input
          id="filtro-calle"
          type="search"
          placeholder="Sobremonte, Constitución…"
          value={borrador.calle}
          onChange={cambiar('calle')}
          className={CLASE_CAMPO}
        />
      </div>

      <div className="flex flex-[1.3_1_200px] flex-col gap-1.5">
        <p id="filtro-precio" className={CLASE_ETIQUETA}>
          Precio
        </p>
        <div role="group" aria-labelledby="filtro-precio" className="flex gap-3.5">
          <input
            id="filtro-precio-min"
            aria-label="Precio desde"
            type="number"
            inputMode="numeric"
            min="0"
            placeholder="Desde"
            value={borrador.precioMin}
            onChange={cambiar('precioMin')}
            className={CLASE_CAMPO}
          />
          <input
            id="filtro-precio-max"
            aria-label="Precio hasta"
            type="number"
            inputMode="numeric"
            min="0"
            placeholder="Hasta"
            value={borrador.precioMax}
            onChange={cambiar('precioMax')}
            className={CLASE_CAMPO}
          />
        </div>
      </div>

      <Desplegable
        id="filtro-moneda"
        etiqueta="Moneda"
        opciones={MONEDAS}
        valor={borrador.moneda}
        onElegir={(valor) => asignar('moneda', valor)}
      />
      <Desplegable
        id="filtro-habitaciones"
        etiqueta="Ambientes"
        opciones={HABITACIONES}
        valor={borrador.habitacionesMin}
        onElegir={(valor) => asignar('habitacionesMin', valor)}
      />

      <div className="flex gap-2 max-sm:w-full">
        <button
          type="submit"
          className="inline-flex h-[50px] items-center justify-center gap-2 rounded-[14px] bg-sol-400 px-7 text-base font-bold text-pino-800 transition hover:bg-sol-500 focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600 focus-visible:ring-offset-2 active:scale-[0.98] max-sm:flex-1"
        >
          <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" className="h-5 w-5">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-3.5-3.5" />
          </svg>
          Buscar
        </button>
      </div>

      {/* Error, aviso y Limpiar van debajo de la tarjeta, fuera de la fila: así nada se mueve al escribir */}
      {(error || precioSinMoneda) && (
        <p
          role={error ? 'alert' : undefined}
          className={`absolute left-5 right-36 top-full mt-3 text-sm ${error ? 'font-bold text-red-700' : 'text-gray-600'}`}
        >
          {error || 'Elegí una moneda para no mezclar precios en pesos y en dólares.'}
        </p>
      )}
      {hayFiltros && (
        <button
          type="button"
          onClick={limpiar}
          className="absolute right-5 top-full mt-2.5 rounded px-1 text-sm font-bold text-gray-600 underline decoration-sol-400 decoration-2 underline-offset-4 hover:text-gray-900 focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600"
        >
          Limpiar filtros
        </button>
      )}
    </form>
  )
}
