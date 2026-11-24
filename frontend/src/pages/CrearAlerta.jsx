import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import CampoFormulario from '../components/CampoFormulario'
import { useAuth } from '../hooks/useAuth'
import { crearAlerta } from '../services/alertas'
import {
  ALERTA_VACIA,
  CLAVES_ALERTA,
  alertaDesdeParametros,
  armarAlertaRequest,
  describirAlerta,
  erroresDelBackend,
  validarAlerta,
} from '../utils/alertas'

const claseSelect = (error) =>
  `mt-1 block w-full rounded-md border bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
    error ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200'
  }`

function enfocarPrimerError(errores) {
  const campo = CLAVES_ALERTA.find((clave) => errores[clave])
  if (campo) document.getElementById(campo)?.focus()
}

function AlertaCreada({ alerta, email, onCrearOtra }) {
  return (
    <div role="status" className="rounded-lg border border-green-200 bg-white p-6 text-center shadow-sm">
      <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-green-100 text-green-600">
        <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-6 w-6">
          <path strokeLinecap="round" strokeLinejoin="round" d="m5 13 4 4L19 7" />
        </svg>
      </div>
      <h2 className="mt-4 text-xl font-semibold text-gray-900">¡Alerta creada!</h2>
      <p className="mt-2 text-sm text-gray-600">
        Te vamos a avisar por email{email ? ` a ${email}` : ''} cuando se publique una propiedad con:
      </p>
      <ul className="mt-3 flex flex-wrap justify-center gap-2">
        {describirAlerta(alerta).map((criterio) => (
          <li key={criterio} className="max-w-full rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-700">
            {criterio}
          </li>
        ))}
      </ul>
      <div className="mt-6 flex flex-wrap justify-center gap-2">
        <button
          type="button"
          onClick={onCrearOtra}
          className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Crear otra alerta
        </button>
        <Link
          to="/alertas"
          className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Ver mis alertas
        </Link>
        <Link to="/" className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
          Ver propiedades
        </Link>
      </div>
    </div>
  )
}

/**
 * Formulario de crear alerta (#85). Consume POST /alertas.
 * Se puede llegar con los criterios ya cargados por URL (?precioMax=...&moneda=...), por ejemplo
 * desde los filtros del listado.
 */
export default function CrearAlerta() {
  const { usuario } = useAuth()
  const [searchParams] = useSearchParams()

  const [datos, setDatos] = useState(() => alertaDesdeParametros(searchParams))
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [creada, setCreada] = useState(null)

  const cambiar = (campo) => (event) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: event.target.value }))
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined }))
    setErrorGeneral(null)
  }

  const campo = (clave) => ({ id: clave, value: datos[clave], onChange: cambiar(clave), error: errores[clave] })

  const enviar = async (event) => {
    event.preventDefault()
    const { porCampo, general } = validarAlerta(datos)
    setErrores(porCampo)
    setErrorGeneral(general)
    if (Object.keys(porCampo).length > 0 || general) {
      enfocarPrimerError(porCampo)
      return
    }

    setEnviando(true)
    try {
      setCreada(await crearAlerta(armarAlertaRequest(datos)))
    } catch (e) {
      // 401: el cliente HTTP ya cierra la sesión y manda al login (SesionExpirada.jsx)
      const { porCampo: delBackend, otros } = erroresDelBackend(e.detalles)
      setErrores(delBackend)
      enfocarPrimerError(delBackend)
      if (otros.length > 0) setErrorGeneral(otros.join('. '))
      else if (Object.keys(delBackend).length === 0) setErrorGeneral(e.mensaje || 'No se pudo crear la alerta')
    } finally {
      setEnviando(false)
    }
  }

  const crearOtra = () => {
    setCreada(null)
    setDatos(ALERTA_VACIA)
    setErrores({})
    setErrorGeneral(null)
  }

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="text-2xl font-bold">Crear alerta</h1>
      <p className="mt-1 text-sm text-gray-600">
        Elegí qué buscás y te avisamos cuando se publique una propiedad que coincida. Todos los criterios son
        opcionales: completá solo los que te importan.
      </p>

      <div className="mt-6">
        {creada ? (
          <AlertaCreada alerta={creada} email={usuario?.email} onCrearOtra={crearOtra} />
        ) : (
          <form
            onSubmit={enviar}
            noValidate
            className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm"
          >
            <fieldset disabled={enviando} className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <div>
                  <label htmlFor="moneda" className="block text-sm font-medium text-gray-700">
                    Moneda
                  </label>
                  <select
                    id="moneda"
                    name="moneda"
                    aria-invalid={!!errores.moneda}
                    aria-describedby={errores.moneda ? 'moneda-error' : undefined}
                    className={claseSelect(errores.moneda)}
                    value={datos.moneda}
                    onChange={cambiar('moneda')}
                  >
                    <option value="">Cualquiera</option>
                    <option value="ARS">Pesos ($)</option>
                    <option value="USD">Dólares (US$)</option>
                  </select>
                  {errores.moneda && (
                    <p id="moneda-error" className="mt-1 text-sm text-red-600">
                      {errores.moneda}
                    </p>
                  )}
                </div>
                <CampoFormulario
                  label="Precio mínimo"
                  type="number"
                  inputMode="decimal"
                  min="0"
                  step="0.01"
                  placeholder="Desde"
                  {...campo('precioMin')}
                />
                <CampoFormulario
                  label="Precio máximo"
                  type="number"
                  inputMode="decimal"
                  min="0"
                  step="0.01"
                  placeholder="Hasta"
                  {...campo('precioMax')}
                />
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <CampoFormulario
                  label="Habitaciones mínimas"
                  type="number"
                  inputMode="numeric"
                  min="1"
                  step="1"
                  placeholder="Ej: 2"
                  {...campo('habitacionesMin')}
                />
                <div className="sm:col-span-2">
                  <CampoFormulario label="Calle" placeholder="Ej: Sobremonte" {...campo('calle')} />
                </div>
              </div>
            </fieldset>

            {errorGeneral && (
              <p role="alert" className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
                {errorGeneral}
              </p>
            )}

            <div className="flex justify-end gap-2">
              <Link to="/" className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100">
                Cancelar
              </Link>
              <button
                type="submit"
                disabled={enviando}
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
              >
                {enviando ? 'Creando...' : 'Crear alerta'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
