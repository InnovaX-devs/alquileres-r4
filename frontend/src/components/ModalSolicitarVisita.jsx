import { useEffect, useId, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { solicitarVisita } from '../services/visitas'
import { aInputFechaHora, validarFechaPropuesta } from '../utils/visitas'

const OBSERVACIONES_MAX = 1000

const claseCampo =
  'mt-1 block w-full rounded-md border px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2'

/**
 * Modal para pedir una visita (#79): fecha propuesta + observaciones → POST /visitas.
 * Se cierra con Escape, con la X o tocando afuera.
 *
 * @param {number}   props.propiedadId
 * @param {string}   props.tituloPropiedad
 * @param {Function} props.onCerrar
 * @param {Function} props.onSolicitada  recibe la solicitud creada (o null si ya existía)
 */
export default function ModalSolicitarVisita({ propiedadId, tituloPropiedad, onCerrar, onSolicitada }) {
  const idTitulo = useId()
  const campoFecha = useRef(null)

  // Mínimo del selector: el momento en que se abrió el modal
  const [minimo] = useState(() => aInputFechaHora(new Date()))
  const [fecha, setFecha] = useState('')
  const [observaciones, setObservaciones] = useState('')
  const [errorFecha, setErrorFecha] = useState(null)
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [enviada, setEnviada] = useState(false)

  // Foco en la fecha al abrir y cierre con Escape
  useEffect(() => {
    campoFecha.current?.focus()
    const alPresionar = (event) => {
      if (event.key === 'Escape') onCerrar()
    }
    document.addEventListener('keydown', alPresionar)
    return () => document.removeEventListener('keydown', alPresionar)
  }, [onCerrar])

  const enviar = async (event) => {
    event.preventDefault()
    setErrorGeneral(null)

    const error = validarFechaPropuesta(fecha)
    setErrorFecha(error)
    if (error) return

    setEnviando(true)
    try {
      const creada = await solicitarVisita({
        propiedadId,
        fechaPropuesta: `${fecha}:00`,
        observaciones: observaciones.trim(),
      })
      setEnviada(true)
      onSolicitada?.(creada)
    } catch (e) {
      if (e.status === 409) {
        setErrorGeneral('Ya solicitaste esta propiedad')
        onSolicitada?.(null)
      } else if (e.status === 404) {
        setErrorGeneral('La propiedad ya no existe o fue dada de baja')
      } else {
        setErrorGeneral(e.detalles?.join('. ') || e.mensaje || 'No se pudo enviar la solicitud')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onCerrar()
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl"
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 id={idTitulo} className="text-lg font-semibold text-gray-900">
              Solicitar visita
            </h2>
            {tituloPropiedad && <p className="mt-0.5 text-sm text-gray-500">{tituloPropiedad}</p>}
          </div>
          <button
            type="button"
            onClick={onCerrar}
            aria-label="Cerrar ventana"
            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
          >
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5">
              <path strokeLinecap="round" d="M6 6l12 12M18 6 6 18" />
            </svg>
          </button>
        </div>

        {enviada ? (
          <div className="mt-6 text-center">
            <p className="font-medium text-green-700">¡Solicitud enviada!</p>
            <p className="mt-1 text-sm text-gray-600">
              Le avisamos al publicador. Vas a ver la respuesta en tus solicitudes.
            </p>
            <div className="mt-6 flex justify-center gap-2">
              <button
                type="button"
                onClick={onCerrar}
                className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Cerrar
              </button>
              <Link
                to="/visitas"
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                Ver mis solicitudes
              </Link>
            </div>
          </div>
        ) : (
          <form onSubmit={enviar} noValidate className="mt-5 space-y-4">
            <div>
              <label htmlFor="visita-fecha" className="block text-sm font-medium text-gray-700">
                Fecha y hora propuesta
              </label>
              <input
                ref={campoFecha}
                id="visita-fecha"
                type="datetime-local"
                min={minimo}
                value={fecha}
                onChange={(e) => {
                  setFecha(e.target.value)
                  setErrorFecha(null)
                }}
                aria-invalid={!!errorFecha}
                aria-describedby={errorFecha ? 'visita-fecha-error' : undefined}
                className={`${claseCampo} ${
                  errorFecha ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200'
                }`}
              />
              {errorFecha && (
                <p id="visita-fecha-error" className="mt-1 text-sm text-red-600">
                  {errorFecha}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="visita-observaciones" className="block text-sm font-medium text-gray-700">
                Observaciones <span className="font-normal text-gray-400">(opcional)</span>
              </label>
              <textarea
                id="visita-observaciones"
                rows={3}
                maxLength={OBSERVACIONES_MAX}
                placeholder="Ej: también puedo otro día por la tarde"
                value={observaciones}
                onChange={(e) => setObservaciones(e.target.value)}
                className={`${claseCampo} border-gray-300 focus:border-blue-500 focus:ring-blue-200`}
              />
              <p className="mt-1 text-right text-xs text-gray-400">
                {observaciones.length}/{OBSERVACIONES_MAX}
              </p>
            </div>

            {errorGeneral && (
              <p role="alert" className="text-sm text-red-600">
                {errorGeneral}
              </p>
            )}

            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={onCerrar}
                className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={enviando}
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
              >
                {enviando ? 'Enviando...' : 'Enviar solicitud'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
