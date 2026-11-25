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
    <Link
      to="/"
      className="-ml-1 inline-flex min-h-11 items-center gap-1.5 rounded-lg px-1 text-[15px] font-bold text-rio-700 hover:text-pino-800 focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600"
    >
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" className="h-[18px] w-[18px]">
        <path d="m15 18-6-6 6-6" />
      </svg>
      Volver a las propiedades
    </Link>
  )
}

function NoEncontrada() {
  return (
    <div className="py-16 text-center">
      <p className="font-display text-7xl font-extrabold text-rio-600">404</p>
      <h1 className="mt-4 text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em]">Propiedad no encontrada</h1>
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
      <div className="mt-2 h-10 w-2/3 rounded-xl bg-gray-200" />
      <div className="mt-3 h-5 w-1/3 rounded-lg bg-gray-200" />
      <div className="mt-6 aspect-[16/9] w-full rounded-3xl bg-gray-200" />
    </div>
  )
}

function Caracteristica({ children }) {
  return <li className="rounded-xl bg-white px-4 py-3 text-[15px] font-bold text-gray-800">{children}</li>
}

function Bloque({ titulo, children }) {
  return (
    <section className="mt-10">
      <h2 className="text-2xl font-bold text-gray-900">{titulo}</h2>
      <div className="mt-3 max-w-[66ch] whitespace-pre-line text-[17px] leading-relaxed text-gray-700">{children}</div>
    </section>
  )
}

function Ubicacion({ children }) {
  return (
    <p className="mt-2 flex items-center gap-1.5 text-lg text-gray-600">
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5 shrink-0">
        <path d="M12 21s-7-6.2-7-11.5a7 7 0 1 1 14 0C19 14.8 12 21 12 21Z" />
        <circle cx="12" cy="9.5" r="2.5" />
      </svg>
      {children}
    </p>
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

      <header className="mt-4">
        <h1 className="max-w-[24ch] text-[clamp(1.9rem,4vw,2.75rem)] font-extrabold leading-[1.08] tracking-[-0.03em] text-gray-900">
          {titulo || ubicacion}
        </h1>
        {titulo && <Ubicacion>{ubicacion}</Ubicacion>}
      </header>

      <div className="mt-6">
        <GaleriaImagenes key={propiedad.id} imagenes={imagenes} titulo={titulo} />
      </div>

      <div className="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_360px] lg:gap-12">
        {/* En el celular el precio y la acción van primero, justo después de las fotos */}
        <aside className="min-w-0 space-y-4 lg:sticky lg:top-6 lg:order-last lg:self-start">
          <div className="rounded-3xl bg-white p-6 shadow-xl shadow-pino-900/10">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-display text-[2.5rem] leading-none font-extrabold tracking-[-0.03em] text-gray-900 tabular-nums">
                  {formatearPrecio(precio, moneda)}
                </p>
                <p className="mt-2 font-display text-[15px] text-gray-600 tabular-nums">
                  por mes
                  {expensas > 0 && `, + ${formatearPrecio(expensas, moneda)} de expensas`}
                  {expensas === 0 && ', sin expensas'}
                </p>
              </div>
              <BotonFavorito tipo="propiedad" id={propiedad.id} className="shrink-0 border-2 border-gray-200 shadow-none" />
            </div>
            <div className="mt-5">
              <SolicitarVisita propiedadId={propiedad.id} tituloPropiedad={titulo} />
            </div>
          </div>
          <ContactoPublicador contacto={contacto} />
        </aside>

        <div className="min-w-0">
          {caracteristicas.length > 0 && (
            <ul className="flex flex-wrap gap-2" aria-label="Características">
              {caracteristicas.map((c) => (
                <Caracteristica key={c}>{c}</Caracteristica>
              ))}
            </ul>
          )}

          {descripcion?.trim() && <Bloque titulo="Descripción">{descripcion}</Bloque>}
          {requisitosIngreso?.trim() && <Bloque titulo="Requisitos para entrar">{requisitosIngreso}</Bloque>}
        </div>
      </div>
    </article>
  )
}
