import { useEffect, useState } from 'react'
import TarjetaPropiedad from '../components/TarjetaPropiedad'
import { ErrorConReintento, Vacio } from '../components/EstadoCarga'
import { listarMisPropiedades } from '../services/propiedades'
import { Button, CLASES_CARD } from '../components/ui'

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

// Propiedades publicadas por el usuario o por su inmobiliaria (#78)
export default function MisPropiedades() {
  const [resultado, setResultado] = useState(null)
  const [intento, setIntento] = useState(0)

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
          <li key={propiedad.id}>
            <TarjetaPropiedad propiedad={propiedad} conFavorito={false} />
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
    </div>
  )
}
