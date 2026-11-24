import { useEffect, useState } from 'react'
import TarjetaPropiedad from '../components/TarjetaPropiedad'
import BotonFavorito from '../components/BotonFavorito'
import { Cargando, ErrorConReintento, Vacio } from '../components/EstadoCarga'
import { useFavoritos } from '../hooks/useFavoritos'
import { listarFavoritos } from '../services/favoritos'

// GET /favoritos trae un resumen de la propiedad: se adapta a lo que espera la tarjeta del listado
function aTarjeta(favorito) {
  return {
    ...favorito,
    id: favorito.propiedadId,
    imagenes: favorito.imagenPrincipal ? [favorito.imagenPrincipal] : [],
  }
}

function Seccion({ titulo, cantidad, children }) {
  return (
    <section className="mt-8 first:mt-0">
      <h2 className="mb-4 text-lg font-semibold text-gray-900">
        {titulo} <span className="text-sm font-normal text-gray-500">({cantidad})</span>
      </h2>
      {children}
    </section>
  )
}

function TarjetaInmobiliaria({ favorito }) {
  const { inmobiliariaId, nombre, logoUrl, verificada } = favorito
  const [logoRoto, setLogoRoto] = useState(false)

  return (
    <li className="flex items-center gap-3 rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      {logoUrl && !logoRoto ? (
        <img
          src={logoUrl}
          alt={`Logo de ${nombre}`}
          onError={() => setLogoRoto(true)}
          className="h-12 w-12 shrink-0 rounded-full border border-gray-200 object-cover"
        />
      ) : (
        <div aria-hidden="true" className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-blue-100 text-lg font-semibold text-blue-700">
          {nombre?.trim()?.[0]?.toUpperCase() || '?'}
        </div>
      )}
      <div className="min-w-0 flex-1">
        <p className="truncate font-medium text-gray-900">{nombre}</p>
        {verificada && <p className="text-xs text-green-700">Inmobiliaria verificada</p>}
      </div>
      <BotonFavorito tipo="inmobiliaria" id={inmobiliariaId} className="border border-gray-200" />
    </li>
  )
}

// Propiedades e inmobiliarias guardadas en favoritos (#84). Se quitan con el mismo corazón (#82 / #83).
export default function Favoritos() {
  const { esFavorito } = useFavoritos()
  const [resultado, setResultado] = useState(null)
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let cancelado = false
    listarFavoritos()
      .then((datos) => {
        if (!cancelado) {
          setResultado({
            intento,
            propiedades: datos?.propiedades ?? [],
            inmobiliarias: datos?.inmobiliarias ?? [],
            error: null,
          })
        }
      })
      .catch((e) => {
        if (!cancelado) {
          setResultado({ intento, propiedades: [], inmobiliarias: [], error: e.mensaje || 'No se pudieron cargar tus favoritos' })
        }
      })
    return () => {
      cancelado = true
    }
  }, [intento])

  // Al tocar el corazón se quita del contexto de favoritos: desaparece del listado sin volver a pedirlo
  const propiedades = (resultado?.propiedades ?? []).filter((f) => esFavorito('propiedad', f.propiedadId) !== false)
  const inmobiliarias = (resultado?.inmobiliarias ?? []).filter(
    (f) => esFavorito('inmobiliaria', f.inmobiliariaId) !== false,
  )

  let contenido
  if (!resultado || resultado.intento !== intento) {
    contenido = <Cargando etiqueta="Cargando tus favoritos" />
  } else if (resultado.error) {
    contenido = <ErrorConReintento mensaje={resultado.error} onReintentar={() => setIntento((n) => n + 1)} />
  } else if (propiedades.length === 0 && inmobiliarias.length === 0) {
    contenido = (
      <Vacio
        titulo="Todavía no guardaste favoritos"
        texto="Tocá el corazón de una propiedad o inmobiliaria para guardarla acá."
        accion="Ver propiedades"
        to="/"
      />
    )
  } else {
    contenido = (
      <>
        {propiedades.length > 0 && (
          <Seccion titulo="Propiedades" cantidad={propiedades.length}>
            <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {propiedades.map((f) => (
                <li key={f.favoritoId}>
                  <TarjetaPropiedad propiedad={aTarjeta(f)} />
                </li>
              ))}
            </ul>
          </Seccion>
        )}
        {inmobiliarias.length > 0 && (
          <Seccion titulo="Inmobiliarias" cantidad={inmobiliarias.length}>
            <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {inmobiliarias.map((f) => (
                <TarjetaInmobiliaria key={f.favoritoId} favorito={f} />
              ))}
            </ul>
          </Seccion>
        )}
      </>
    )
  }

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">Favoritos</h1>
      {contenido}
    </div>
  )
}
