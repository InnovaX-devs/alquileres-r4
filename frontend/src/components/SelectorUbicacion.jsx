import { useEffect, useState } from 'react'
import { MapaSelector } from './mapa'
import { sugerirUbicacion } from '../services/geocodificacion'

const ESPERA_MS = 700 // se pide la sugerencia cuando el usuario deja de escribir

/**
 * Selector de ubicación del formulario de publicar (#141).
 *
 * Al completar calle y altura se pide una sugerencia al backend (#140) y se muestra el mapa con un
 * pin arrastrable. Si no hay sugerencia, el mapa arranca centrado en Río Cuarto sin pin, y el
 * usuario lo pone tocando el mapa: así nunca se guarda una ubicación que nadie confirmó.
 * La ubicación que se guarda es la que dejó el usuario.
 *
 * @param {{latitud, longitud} | null} props.ubicacion
 * @param {Function} props.onCambiar  recibe { latitud, longitud } o null
 */
export default function SelectorUbicacion({ calle, altura, ubicacion, onCambiar }) {
  const direccion = `${calle.trim()}|${altura.trim()}`
  const completa = calle.trim() !== '' && altura.trim() !== ''

  // Resultado de la última consulta y para qué dirección fue
  const [consulta, setConsulta] = useState({ direccion: null, estado: null }) // estado: 'sugerida' | 'sin-sugerencia'
  // De dónde salió el pin. Si la dirección nueva no tiene sugerencia, un pin que puso el usuario
  // se mantiene; uno que venía de la sugerencia de la dirección anterior se quita.
  const [origen, setOrigen] = useState(null) // 'sugerencia' | 'usuario' | null
  // Cambia con cada sugerencia, para recentrar el mapa
  const [version, setVersion] = useState(0)

  useEffect(() => {
    if (!completa) return
    let vigente = true
    const timer = setTimeout(() => {
      sugerirUbicacion(calle.trim(), altura.trim())
        .then((sugerencia) => {
          if (!vigente) return
          if (sugerencia) {
            onCambiar({ latitud: sugerencia.latitud, longitud: sugerencia.longitud })
            setOrigen('sugerencia')
            setVersion((v) => v + 1)
            setConsulta({ direccion, estado: 'sugerida' })
          } else {
            // Un pin que venía de la sugerencia de otra dirección ya no sirve
            if (origen !== 'usuario') {
              onCambiar(null)
              setOrigen(null)
            }
            setConsulta({ direccion, estado: 'sin-sugerencia' })
          }
        })
        .catch(() => {
          if (vigente) setConsulta({ direccion, estado: 'sin-sugerencia' })
        })
    }, ESPERA_MS)
    return () => {
      vigente = false
      clearTimeout(timer)
    }
    // Solo cuando cambia la dirección (onCambiar y origen no deben volver a disparar la consulta)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [direccion, completa])

  if (!completa) {
    return (
      <p className="rounded-lg bg-gray-50 px-4 py-3 text-sm text-gray-600">
        Completá la calle y la altura para marcar la propiedad en el mapa.
      </p>
    )
  }

  const buscando = consulta.direccion !== direccion
  const elegirEnMapa = (nueva) => {
    onCambiar(nueva)
    setOrigen('usuario')
  }

  let mensaje
  if (buscando) mensaje = 'Buscando la dirección en el mapa...'
  else if (ubicacion) mensaje = 'Revisá que el pin esté en tu propiedad. Si no, arrastralo.'
  else mensaje = 'No encontramos la dirección. Tocá el mapa en el lugar de tu propiedad para marcarla.'

  return (
    <div className="space-y-3">
      <p role="status" className="text-sm text-gray-700">
        {mensaje}
      </p>
      <MapaSelector ubicacion={ubicacion} version={version} onCambiar={elegirEnMapa} />
      <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-gray-500">
        <span>La ubicación exacta solo la ves vos. En la publicación se muestra una zona aproximada.</span>
        {ubicacion && (
          <button
            type="button"
            onClick={() => {
              onCambiar(null)
              setOrigen(null)
            }}
            className="font-medium text-red-600 hover:underline"
          >
            Quitar ubicación
          </button>
        )}
      </div>
    </div>
  )
}
