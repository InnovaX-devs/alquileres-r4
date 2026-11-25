import { useEffect, useState } from 'react'
import { Cargando, ErrorConReintento, Vacio } from '../components/EstadoCarga'
import { cambiarEstadoAlerta, eliminarAlerta, listarAlertas } from '../services/alertas'
import { describirAlerta } from '../utils/alertas'
import { formatearFechaHora } from '../utils/visitas'
import { Button, Card } from '../components/ui'
import { useToast } from '../hooks/useToast'

function Interruptor({ activa, deshabilitado, onCambiar }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={activa}
      aria-label={activa ? 'Alerta activa. Tocá para pausarla' : 'Alerta pausada. Tocá para activarla'}
      onClick={onCambiar}
      disabled={deshabilitado}
      className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-rio-500 focus-visible:ring-offset-2 disabled:opacity-60 ${
        activa ? 'bg-rio-600' : 'bg-gray-300'
      }`}
    >
      <span
        aria-hidden="true"
        className={`inline-block h-5 w-5 rounded-full bg-white shadow transition-transform ${
          activa ? 'translate-x-5' : 'translate-x-0.5'
        }`}
      />
    </button>
  )
}

function TarjetaAlerta({ alerta, onActualizada, onEliminada }) {
  const { id, activa, creadaEn } = alerta
  const [guardando, setGuardando] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const toast = useToast()

  const alternar = async () => {
    setGuardando(true)
    try {
      const actualizada = await cambiarEstadoAlerta(id, !activa)
      const quedoActiva = actualizada?.activa ?? !activa
      onActualizada({ ...alerta, ...actualizada, activa: quedoActiva })
      toast.exito(quedoActiva ? 'Alerta activada' : 'Alerta pausada')
    } catch (e) {
      if (e.status === 404) {
        onEliminada(id) // ya no existe (por ejemplo, se borró desde otra pestaña)
        toast.info('Esa alerta ya no existe')
      } else {
        toast.error(e, 'No se pudo actualizar la alerta')
      }
    } finally {
      setGuardando(false)
    }
  }

  const eliminar = async () => {
    setGuardando(true)
    try {
      await eliminarAlerta(id)
      onEliminada(id)
      toast.exito('Alerta eliminada')
    } catch (e) {
      if (e.status === 404) {
        onEliminada(id)
        toast.exito('Alerta eliminada')
      } else {
        toast.error(e, 'No se pudo eliminar la alerta')
        setGuardando(false)
        setConfirmando(false)
      }
    }
  }

  const criterios = describirAlerta(alerta)

  return (
    // Pausada: fondo gris (con ! para ganarle al bg-white de Card)
    <Card as="li" padding="sm" className={activa ? '' : 'bg-gray-50!'}>
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <ul className="flex flex-wrap gap-2" aria-label="Criterios de la alerta">
            {criterios.map((criterio) => (
              <li
                key={criterio}
                className={`max-w-full rounded-full px-3 py-1 text-sm font-bold ${
                  activa ? 'bg-rio-50 text-rio-700' : 'bg-gray-100 text-gray-500'
                }`}
              >
                {criterio}
              </li>
            ))}
          </ul>
          <p className="mt-2 text-xs text-gray-500">
            {activa ? 'Activa' : 'Pausada'} · Creada el {formatearFechaHora(creadaEn)}
          </p>
        </div>
        <Interruptor activa={activa} deshabilitado={guardando} onCambiar={alternar} />
      </div>

      <div className="mt-3 flex flex-wrap items-center justify-end gap-2">
        {confirmando ? (
          <>
            <span className="text-sm text-gray-700">¿Eliminar esta alerta?</span>
            <Button variante="secundario" tamanio="sm" onClick={() => setConfirmando(false)} disabled={guardando}>
              Cancelar
            </Button>
            <Button variante="peligro" tamanio="sm" onClick={eliminar} cargando={guardando}>
              {guardando ? 'Eliminando...' : 'Sí, eliminar'}
            </Button>
          </>
        ) : (
          <Button variante="peligroSuave" tamanio="sm" onClick={() => setConfirmando(true)} disabled={guardando}>
            Eliminar
          </Button>
        )}
      </div>
    </Card>
  )
}

// Gestión de alertas (#86): listado con activar/pausar y eliminar
export default function MisAlertas() {
  const [resultado, setResultado] = useState(null)
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let cancelado = false
    listarAlertas()
      .then((datos) => {
        if (!cancelado) setResultado({ intento, alertas: datos ?? [], error: null })
      })
      .catch((e) => {
        if (!cancelado) setResultado({ intento, alertas: [], error: e.mensaje || 'No se pudieron cargar tus alertas' })
      })
    return () => {
      cancelado = true
    }
  }, [intento])

  const actualizar = (alerta) =>
    setResultado((anterior) => ({
      ...anterior,
      alertas: anterior.alertas.map((a) => (a.id === alerta.id ? alerta : a)),
    }))

  const quitar = (id) =>
    setResultado((anterior) => ({ ...anterior, alertas: anterior.alertas.filter((a) => a.id !== id) }))

  let contenido
  if (!resultado || resultado.intento !== intento) {
    contenido = <Cargando etiqueta="Cargando tus alertas" />
  } else if (resultado.error) {
    contenido = <ErrorConReintento mensaje={resultado.error} onReintentar={() => setIntento((n) => n + 1)} />
  } else if (resultado.alertas.length === 0) {
    contenido = (
      <Vacio
        titulo="Todavía no tenés alertas"
        texto="Creá una alerta y te avisamos por email cuando se publique una propiedad que te interese."
        accion="Crear alerta"
        to="/alertas/nueva"
      />
    )
  } else {
    const activas = resultado.alertas.filter((a) => a.activa).length
    contenido = (
      <>
        <p className="mb-4 text-sm text-gray-600">
          {activas === 0
            ? 'Todas tus alertas están pausadas: no vas a recibir avisos.'
            : `Te avisamos por email cuando se publique una propiedad que coincida con ${
                activas === 1 ? 'tu alerta activa' : `alguna de tus ${activas} alertas activas`
              }.`}
        </p>
        <ul className="space-y-3">
          {resultado.alertas.map((alerta) => (
            <TarjetaAlerta key={alerta.id} alerta={alerta} onActualizada={actualizar} onEliminada={quitar} />
          ))}
        </ul>
      </>
    )
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em]">Mis alertas</h1>
        <Button to="/alertas/nueva">Nueva alerta</Button>
      </div>
      {contenido}
    </div>
  )
}
