import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { obtenerPropiedad } from '../services/propiedades'
import GaleriaImagenes from '../components/GaleriaImagenes'
import ContactoPublicador from '../components/ContactoPublicador'
import SolicitarVisita from '../components/SolicitarVisita'
import BotonFavorito from '../components/BotonFavorito'
import { ErrorConReintento } from '../components/EstadoCarga'
import { Aviso, Button } from '../components/ui'
import {
  formatearHabitaciones,
  formatearImpuestos,
  formatearPlazoActualizacion,
  formatearPrecio,
  formatearUbicacion,
} from '../utils/propiedad'

function VolverAlListado() {
  return (
    <Link to="/" className="inline-flex items-center gap-1 text-sm text-blue-600 hover:underline">
      <span aria-hidden="true">←</span> Volver a las propiedades
    </Link>
  )
}

function NoEncontrada() {
  return (
    <div className="py-16 text-center">
      <p className="text-6xl font-bold text-blue-600">404</p>
      <h1 className="mt-4 text-2xl font-semibold">Propiedad no encontrada</h1>
      <p className="mt-2 text-gray-500">La propiedad que buscás no existe o ya no está publicada.</p>
      <Button to="/" className="mt-6">
        Ver todas las propiedades
      </Button>
    </div>
  )
}

function Cargando() {
  return (
    <div role="status" aria-label="Cargando propiedad" className="animate-pulse">
      <div className="aspect-[16/10] w-full rounded-lg bg-gray-200" />
      <div className="mt-6 h-8 w-1/3 rounded bg-gray-200" />
      <div className="mt-3 h-5 w-1/2 rounded bg-gray-200" />
      <div className="mt-6 h-24 w-full rounded bg-gray-200" />
    </div>
  )
}

function Caracteristica({ children }) {
  return (
    <li className="rounded-full border border-gray-200 bg-white px-3 py-1 text-sm text-gray-700">{children}</li>
  )
}

function Bloque({ titulo, children }) {
  return (
    <section className="mt-8">
      <h2 className="text-lg font-semibold text-gray-900">{titulo}</h2>
      <div className="mt-2 whitespace-pre-line text-gray-700">{children}</div>
    </section>
  )
}

// Detalle de propiedad (#74) con la sección de contacto del publicador (#75)
export default function PropiedadDetalle() {
  const { id } = useParams()
  // Viene del formulario de publicar (#76): se muestra un aviso de éxito
  const { state } = useLocation()
  const recienPublicada = state?.publicada === true
  const imagenesSinSubir = state?.imagenesSinSubir ?? 0 // fotos que fallaron al publicar (#77)
  // El contacto depende de quién mira: se vuelve a pedir al iniciar o cerrar sesión
  const { token } = useAuth()

  // Resultado de la última carga, con el id al que corresponde:
  // { id, estado: 'ok' | 'no-encontrada' | 'error', propiedad?, error? }
  const [resultado, setResultado] = useState(null)
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    let cancelado = false
    obtenerPropiedad(id)
      .then((datos) => {
        if (!cancelado) setResultado({ id, estado: 'ok', propiedad: datos })
      })
      .catch((e) => {
        if (cancelado) return
        // 400: el id no es un número (ej. /propiedades/abc)
        if (e.status === 404 || e.status === 400) {
          setResultado({ id, estado: 'no-encontrada' })
        } else {
          setResultado({ id, estado: 'error', error: e.mensaje || 'No se pudo cargar la propiedad' })
        }
      })
    return () => {
      cancelado = true
    }
  }, [id, token, intento])

  const reintentar = () => {
    setResultado(null)
    setIntento((n) => n + 1)
  }

  // Sin resultado, o con el de otra propiedad (al navegar entre propiedades): cargando.
  // Al iniciar o cerrar sesión se sigue mostrando la misma propiedad mientras se actualiza el contacto.
  if (!resultado || resultado.id !== id) return <Cargando />
  if (resultado.estado === 'no-encontrada') return <NoEncontrada />
  if (resultado.estado === 'error') {
    return <ErrorConReintento mensaje={resultado.error} onReintentar={reintentar} />
  }

  const { propiedad } = resultado
  const {
    titulo,
    descripcion,
    precio,
    expensas,
    moneda,
    habitaciones,
    metrosCuadrados,
    incluyeImpuestos,
    plazoActualizacionMeses,
    requisitosIngreso,
    imagenes,
    contacto,
  } = propiedad

  const ubicacion = formatearUbicacion(propiedad)
  const caracteristicas = [
    formatearHabitaciones(habitaciones),
    metrosCuadrados ? `${metrosCuadrados} m²` : null,
    formatearImpuestos(incluyeImpuestos),
    formatearPlazoActualizacion(plazoActualizacionMeses),
  ].filter(Boolean)

  return (
    <article>
      <VolverAlListado />

      {recienPublicada && (
        <Aviso tipo="exito" className="mt-4">
          ¡Listo! Tu propiedad ya está publicada.
          {imagenesSinSubir > 0 &&
            ` Quedaron ${imagenesSinSubir} ${imagenesSinSubir === 1 ? 'foto' : 'fotos'} sin subir.`}
        </Aviso>
      )}

      <div className="mt-4 grid gap-8 lg:grid-cols-3">
        <div className="min-w-0 lg:col-span-2">
          <GaleriaImagenes key={propiedad.id} imagenes={imagenes} titulo={titulo} />

          <header className="mt-6">
            <div className="flex items-start justify-between gap-4">
              <p className="text-3xl font-bold text-gray-900">{formatearPrecio(precio, moneda)}</p>
              <BotonFavorito tipo="propiedad" id={propiedad.id} className="border border-gray-200" />
            </div>
            {expensas > 0 && (
              <p className="mt-1 text-sm text-gray-500">+ {formatearPrecio(expensas, moneda)} de expensas</p>
            )}
            <h1 className="mt-3 text-2xl font-semibold text-gray-900">{titulo || ubicacion}</h1>
            {titulo && <p className="mt-1 text-gray-600">{ubicacion}</p>}
          </header>

          {caracteristicas.length > 0 && (
            <ul className="mt-4 flex flex-wrap gap-2" aria-label="Características">
              {caracteristicas.map((c) => (
                <Caracteristica key={c}>{c}</Caracteristica>
              ))}
            </ul>
          )}

          {descripcion?.trim() && <Bloque titulo="Descripción">{descripcion}</Bloque>}
          {requisitosIngreso?.trim() && <Bloque titulo="Requisitos de ingreso">{requisitosIngreso}</Bloque>}
        </div>

        <aside className="min-w-0 space-y-4 lg:sticky lg:top-6 lg:self-start">
          <SolicitarVisita propiedadId={propiedad.id} tituloPropiedad={titulo} />
          <ContactoPublicador contacto={contacto} />
        </aside>
      </div>
    </article>
  )
}
