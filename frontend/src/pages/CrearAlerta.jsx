import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Aviso, Button, Card, Input, Select } from '../components/ui'
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

function enfocarPrimerError(errores) {
  const campo = CLAVES_ALERTA.find((clave) => errores[clave])
  if (campo) document.getElementById(campo)?.focus()
}

function AlertaCreada({ alerta, email, onCrearOtra }) {
  return (
    <Card role="status" className="text-center">
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
        <Button variante="secundario" onClick={onCrearOtra}>
          Crear otra alerta
        </Button>
        <Button variante="secundario" to="/alertas">
          Ver mis alertas
        </Button>
        <Button to="/">Ver propiedades</Button>
      </div>
    </Card>
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
          <Card as="form" onSubmit={enviar} noValidate className="space-y-4">
            <fieldset disabled={enviando} className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <Select label="Moneda" {...campo('moneda')}>
                  <option value="">Cualquiera</option>
                  <option value="ARS">Pesos ($)</option>
                  <option value="USD">Dólares (US$)</option>
                </Select>
                <Input
                  label="Precio mínimo"
                  type="number"
                  inputMode="decimal"
                  min="0"
                  step="0.01"
                  placeholder="Desde"
                  {...campo('precioMin')}
                />
                <Input
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
                <Input
                  label="Habitaciones mínimas"
                  type="number"
                  inputMode="numeric"
                  min="1"
                  step="1"
                  placeholder="Ej: 2"
                  {...campo('habitacionesMin')}
                />
                <div className="sm:col-span-2">
                  <Input label="Calle" placeholder="Ej: Sobremonte" {...campo('calle')} />
                </div>
              </div>
            </fieldset>

            {errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}

            <div className="flex justify-end gap-2">
              <Button variante="fantasma" to="/">
                Cancelar
              </Button>
              <Button type="submit" cargando={enviando}>
                {enviando ? 'Creando...' : 'Crear alerta'}
              </Button>
            </div>
          </Card>
        )}
      </div>
    </div>
  )
}
