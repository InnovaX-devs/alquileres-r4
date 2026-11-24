import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import CampoFormulario from '../components/CampoFormulario'
import { publicarPropiedad } from '../services/propiedades'
import {
  MONEDAS,
  OPCIONES_IMPUESTOS,
  PROPIEDAD_VACIA,
  armarPropiedadRequest,
  erroresDelBackend,
  validarPropiedad,
} from '../utils/formularioPropiedad'

// PROPIEDAD_VACIA tiene los campos en el orden de la pantalla: al haber errores se enfoca el primero
const ORDEN_CAMPOS = Object.keys(PROPIEDAD_VACIA)

const claseControl = (error) =>
  `mt-1 block w-full rounded-md border bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
    error ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200'
  }`

function Etiqueta({ id, label, opcional }) {
  return (
    <label htmlFor={id} className="block text-sm font-medium text-gray-700">
      {label} {opcional && <span className="font-normal text-gray-400">(opcional)</span>}
    </label>
  )
}

function MensajeError({ id, error }) {
  if (!error) return null
  return (
    <p id={`${id}-error`} className="mt-1 text-sm text-red-600">
      {error}
    </p>
  )
}

function CampoSelect({ id, label, opcional, error, opciones, ...props }) {
  return (
    <div>
      <Etiqueta id={id} label={label} opcional={opcional} />
      <select
        id={id}
        name={id}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : undefined}
        className={claseControl(error)}
        {...props}
      >
        {opciones.map(({ valor, label: texto }) => (
          <option key={valor} value={valor}>
            {texto}
          </option>
        ))}
      </select>
      <MensajeError id={id} error={error} />
    </div>
  )
}

function CampoTexto({ id, label, opcional, error, ...props }) {
  return (
    <div>
      <Etiqueta id={id} label={label} opcional={opcional} />
      <textarea
        id={id}
        name={id}
        rows={4}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : undefined}
        className={claseControl(error)}
        {...props}
      />
      <MensajeError id={id} error={error} />
    </div>
  )
}

function Seccion({ titulo, children }) {
  return (
    <fieldset className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <legend className="px-1 text-base font-semibold text-gray-900">{titulo}</legend>
      {children}
    </fieldset>
  )
}

// Agrega "(opcional)" al label de los inputs comunes sin tocar CampoFormulario
const opcional = (texto) => (
  <>
    {texto} <span className="font-normal text-gray-400">(opcional)</span>
  </>
)

function enfocarPrimerError(errores) {
  const campo = ORDEN_CAMPOS.find((clave) => errores[clave])
  if (campo) document.getElementById(campo)?.focus()
}

// Formulario de publicar propiedad (#76). Consume POST /propiedades y redirige al detalle.
export default function PublicarPropiedad() {
  const navigate = useNavigate()

  const [datos, setDatos] = useState(PROPIEDAD_VACIA)
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo) => (event) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: event.target.value }))
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined }))
  }

  // Props comunes de cada campo
  const campo = (clave) => ({ id: clave, value: datos[clave], onChange: cambiar(clave), error: errores[clave] })

  const enviar = async (event) => {
    event.preventDefault()
    setErrorGeneral(null)

    const encontrados = validarPropiedad(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) {
      enfocarPrimerError(encontrados)
      return
    }

    setEnviando(true)
    try {
      const creada = await publicarPropiedad(armarPropiedadRequest(datos))
      navigate(`/propiedades/${creada.id}`, { replace: true, state: { publicada: true } })
    } catch (e) {
      // 401: el cliente HTTP ya cierra la sesión y manda al login (SesionExpirada.jsx)
      const { porCampo, otros } = erroresDelBackend(e.detalles)
      setErrores(porCampo)
      enfocarPrimerError(porCampo)
      if (Object.keys(porCampo).length === 0 || otros.length > 0) {
        setErrorGeneral(otros.length > 0 ? otros.join('. ') : e.mensaje || 'No se pudo publicar la propiedad')
      }
      setEnviando(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-2xl font-bold">Publicar propiedad</h1>
      <p className="mt-1 text-sm text-gray-600">
        Los campos marcados como opcionales se pueden dejar vacíos.
      </p>

      <form onSubmit={enviar} noValidate className="mt-6 space-y-6">
        <Seccion titulo="Datos principales">
          <CampoFormulario label="Título" placeholder="Ej: Departamento 2 ambientes con balcón" {...campo('titulo')} />
          <CampoTexto
            label="Descripción"
            opcional
            placeholder="Contá cómo es la propiedad, qué tiene cerca, estado, etc."
            {...campo('descripcion')}
          />
        </Seccion>

        <Seccion titulo="Precio">
          <div className="grid gap-4 sm:grid-cols-2">
            <CampoFormulario
              label="Precio mensual"
              type="number"
              inputMode="decimal"
              min="0"
              step="0.01"
              placeholder="Ej: 350000"
              {...campo('precio')}
            />
            <CampoSelect label="Moneda" opciones={MONEDAS} {...campo('moneda')} />
            <CampoFormulario
              label={opcional('Expensas')}
              type="number"
              inputMode="decimal"
              min="0"
              step="0.01"
              placeholder="En la misma moneda del precio"
              {...campo('expensas')}
            />
            <CampoSelect
              label="¿El precio incluye impuestos?"
              opcional
              opciones={OPCIONES_IMPUESTOS}
              {...campo('incluyeImpuestos')}
            />
            <CampoFormulario
              label={opcional('Actualización del precio (meses)')}
              type="number"
              inputMode="numeric"
              min="1"
              step="1"
              placeholder="Ej: 3"
              {...campo('plazoActualizacionMeses')}
            />
          </div>
        </Seccion>

        <Seccion titulo="Ubicación">
          <div className="grid gap-4 sm:grid-cols-3">
            <div className="sm:col-span-2">
              <CampoFormulario label="Calle" autoComplete="address-line1" placeholder="Ej: Sobremonte" {...campo('calle')} />
            </div>
            <CampoFormulario label="Altura" placeholder="Ej: 1234" {...campo('altura')} />
            <CampoFormulario label={opcional('Piso / depto')} placeholder="Ej: 5B" {...campo('piso')} />
          </div>
        </Seccion>

        <Seccion titulo="Características">
          <div className="grid gap-4 sm:grid-cols-2">
            <CampoFormulario
              label={opcional('Habitaciones')}
              type="number"
              inputMode="numeric"
              min="0"
              step="1"
              placeholder="0 = monoambiente"
              {...campo('habitaciones')}
            />
            <CampoFormulario
              label={opcional('Metros cuadrados')}
              type="number"
              inputMode="numeric"
              min="1"
              step="1"
              placeholder="Ej: 45"
              {...campo('metrosCuadrados')}
            />
          </div>
          <CampoTexto
            label="Requisitos de ingreso"
            opcional
            placeholder="Ej: garantía propietaria, recibo de sueldo, depósito"
            {...campo('requisitosIngreso')}
          />
        </Seccion>

        {errorGeneral && (
          <p role="alert" className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
            {errorGeneral}
          </p>
        )}

        <div className="flex justify-end gap-2">
          <Link
            to="/"
            className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={enviando}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
          >
            {enviando ? 'Publicando...' : 'Publicar propiedad'}
          </button>
        </div>
      </form>
    </div>
  )
}
