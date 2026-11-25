import { useState } from 'react'
import { solicitarVisita } from '../services/visitas'
import { aInputFechaHora, validarFechaPropuesta } from '../utils/visitas'
import { Aviso, Button, Input, Modal, Textarea } from './ui'

const OBSERVACIONES_MAX = 1000

/**
 * Modal para pedir una visita (#79): fecha propuesta + observaciones → POST /visitas.
 * Se cierra con Escape, con la X o tocando afuera (lo maneja Modal).
 *
 * @param {number}   props.propiedadId
 * @param {string}   props.tituloPropiedad
 * @param {Function} props.onCerrar
 * @param {Function} props.onSolicitada  recibe la solicitud creada (o null si ya existía)
 */
export default function ModalSolicitarVisita({ propiedadId, tituloPropiedad, onCerrar, onSolicitada }) {
  // Mínimo del selector: el momento en que se abrió el modal
  const [minimo] = useState(() => aInputFechaHora(new Date()))
  const [fecha, setFecha] = useState('')
  const [observaciones, setObservaciones] = useState('')
  const [errorFecha, setErrorFecha] = useState(null)
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [enviada, setEnviada] = useState(false)

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
    <Modal titulo="Solicitar visita" subtitulo={tituloPropiedad} onCerrar={onCerrar}>
      {enviada ? (
        <div className="mt-6 text-center">
          <p className="font-medium text-green-700">¡Solicitud enviada!</p>
          <p className="mt-1 text-sm text-gray-600">
            Le avisamos al publicador. Vas a ver la respuesta en tus solicitudes.
          </p>
          <div className="mt-6 flex justify-center gap-2">
            <Button variante="secundario" onClick={onCerrar}>
              Cerrar
            </Button>
            <Button to="/visitas">Ver mis solicitudes</Button>
          </div>
        </div>
      ) : (
        <form onSubmit={enviar} noValidate className="mt-5 space-y-4">
          <Input
            id="visita-fecha"
            label="Fecha y hora propuesta"
            type="datetime-local"
            autoFocus
            min={minimo}
            value={fecha}
            onChange={(e) => {
              setFecha(e.target.value)
              setErrorFecha(null)
            }}
            error={errorFecha}
          />

          <Textarea
            id="visita-observaciones"
            label="Observaciones"
            opcional
            rows={3}
            maxLength={OBSERVACIONES_MAX}
            placeholder="Ej: también puedo otro día por la tarde"
            value={observaciones}
            onChange={(e) => setObservaciones(e.target.value)}
            contador={{ actual: observaciones.length, maximo: OBSERVACIONES_MAX }}
          />

          {errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}

          <div className="flex justify-end gap-2">
            <Button variante="secundario" onClick={onCerrar}>
              Cancelar
            </Button>
            <Button type="submit" cargando={enviando}>
              {enviando ? 'Enviando...' : 'Enviar solicitud'}
            </Button>
          </div>
        </form>
      )}
    </Modal>
  )
}
