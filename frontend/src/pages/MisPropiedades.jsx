import { useEffect, useState } from 'react'
import TarjetaPropiedad from '../components/TarjetaPropiedad'
import { ErrorConReintento, Vacio } from '../components/EstadoCarga'
import { cambiarEstadoPropiedad, eliminarPropiedad, listarMisPropiedades } from '../services/propiedades'
import { useToast } from '../hooks/useToast'
import { Button, CLASES_CARD, Modal } from '../components/ui'

const grilla = 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3'

function Cargando() {
  return (
    <ul role="status" aria-label="Cargando tus propiedades" className={grilla}>
      {[0, 1, 2].map((i) => (
        <li key={i} className={`h-72 animate-pulse ${CLASES_CARD}`} />
      ))}
    </ul>
  )
}

function ConfirmarEliminar({ propiedad, eliminando, onConfirmar, onCerrar }) {
  return (
    <Modal titulo="¿Eliminar la propiedad?" subtitulo={propiedad.titulo} onCerrar={eliminando ? () => {} : onCerrar}>
      <div className="mt-4 space-y-2 text-[15px] text-gray-700">
        <p>Se borra la publicación con sus fotos, y desaparece de los favoritos y de los pedidos de visita.</p>
        <p className="font-bold">No se puede deshacer.</p>
        {propiedad.estado !== 'ALQUILADA' && (
          <p className="text-sm text-gray-600">
            Si solo la alquilaste, mejor marcala como alquilada: deja de mostrarse y la podés volver a publicar.
          </p>
        )}
      </div>
      <div className="mt-6 flex justify-end gap-2">
        <Button variante="secundario" onClick={onCerrar} disabled={eliminando}>
          Cancelar
        </Button>
        <Button variante="peligro" onClick={onConfirmar} cargando={eliminando}>
          {eliminando ? 'Eliminando...' : 'Sí, eliminar'}
        </Button>
      </div>
    </Modal>
  )
}

/** Editar, marcar como alquilada / volver a publicar y eliminar, debajo de cada tarjeta. */
function Acciones({ propiedad, ocupada, onCambiarEstado, onEliminar }) {
  const alquilada = propiedad.estado === 'ALQUILADA'
  return (
    <div className="mt-2 flex flex-wrap gap-1.5">
      <Button variante="secundario" tamanio="sm" to={`/propiedades/${propiedad.id}/editar`}>
        Editar
      </Button>
      <Button variante="suave" tamanio="sm" onClick={onCambiarEstado} cargando={ocupada === 'estado'} disabled={!!ocupada}>
        {alquilada ? 'Volver a publicar' : 'Marcar como alquilada'}
      </Button>
      <Button variante="peligroSuave" tamanio="sm" onClick={onEliminar} disabled={!!ocupada}>
        Eliminar
      </Button>
    </div>
  )
}

// Propiedades publicadas por el usuario o por su inmobiliaria (#78), con sus acciones
export default function MisPropiedades() {
  const toast = useToast()
  const [resultado, setResultado] = useState(null)
  const [intento, setIntento] = useState(0)
  const [ocupadas, setOcupadas] = useState({}) // { [id]: 'estado' | 'eliminar' }
  const [aEliminar, setAEliminar] = useState(null) // propiedad del modal de confirmación

  useEffect(() => {
    let cancelado = false
    listarMisPropiedades()
      .then((datos) => {
        if (!cancelado) setResultado({ intento, propiedades: datos ?? [], error: null })
      })
      .catch((e) => {
        if (!cancelado) setResultado({ intento, propiedades: [], error: e.mensaje || 'No se pudieron cargar tus propiedades' })
      })
    return () => {
      cancelado = true
    }
  }, [intento])

  const cargando = !resultado || resultado.intento !== intento
  const propiedades = resultado?.propiedades ?? []

  const marcarOcupada = (id, accion) => setOcupadas((anteriores) => ({ ...anteriores, [id]: accion }))
  const reemplazar = (id, cambio) =>
    setResultado((anterior) => ({ ...anterior, propiedades: cambio(anterior.propiedades) }))

  const cambiarEstado = async (propiedad) => {
    const nuevo = propiedad.estado === 'ALQUILADA' ? 'DISPONIBLE' : 'ALQUILADA'
    marcarOcupada(propiedad.id, 'estado')
    try {
      const actualizada = await cambiarEstadoPropiedad(propiedad.id, nuevo)
      reemplazar(propiedad.id, (lista) => lista.map((p) => (p.id === propiedad.id ? actualizada : p)))
      toast.exito(
        nuevo === 'ALQUILADA'
          ? 'Listo, la marcaste como alquilada. Ya no aparece en el listado.'
          : 'Listo, la propiedad volvió a estar publicada.',
      )
    } catch (e) {
      toast.error(e, 'No se pudo cambiar el estado de la propiedad')
    } finally {
      marcarOcupada(propiedad.id, undefined)
    }
  }

  const eliminar = async () => {
    const { id } = aEliminar
    marcarOcupada(id, 'eliminar')
    try {
      await eliminarPropiedad(id)
      reemplazar(id, (lista) => lista.filter((p) => p.id !== id))
      setAEliminar(null)
      toast.exito('Propiedad eliminada')
    } catch (e) {
      toast.error(e, 'No se pudo eliminar la propiedad')
    } finally {
      marcarOcupada(id, undefined)
    }
  }

  let contenido
  if (cargando) {
    contenido = <Cargando />
  } else if (resultado.error) {
    contenido = <ErrorConReintento mensaje={resultado.error} onReintentar={() => setIntento((n) => n + 1)} />
  } else if (propiedades.length === 0) {
    contenido = (
      <Vacio
        titulo="Todavía no publicaste propiedades"
        texto="Cuando publiques una, la vas a ver acá."
        accion="Publicar una propiedad"
        to="/publicar"
      />
    )
  } else {
    contenido = (
      <ul className={grilla}>
        {propiedades.map((propiedad) => (
          <li key={propiedad.id} className="flex flex-col">
            <div className="flex-1">
              <TarjetaPropiedad propiedad={propiedad} conFavorito={false} />
            </div>
            <Acciones
              propiedad={propiedad}
              ocupada={ocupadas[propiedad.id]}
              onCambiarEstado={() => cambiarEstado(propiedad)}
              onEliminar={() => setAEliminar(propiedad)}
            />
          </li>
        ))}
      </ul>
    )
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em]">Mis propiedades</h1>
        {!cargando && !resultado.error && propiedades.length > 0 && (
          <Button to="/publicar">Publicar otra</Button>
        )}
      </div>
      {contenido}
      {aEliminar && (
        <ConfirmarEliminar
          propiedad={aEliminar}
          eliminando={ocupadas[aEliminar.id] === 'eliminar'}
          onConfirmar={eliminar}
          onCerrar={() => setAEliminar(null)}
        />
      )}
    </div>
  )
}
