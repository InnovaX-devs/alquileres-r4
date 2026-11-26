import { useRef, useState } from 'react'
import {
  MAXIMO_IMAGENES,
  TAMANIO_MAXIMO_MB,
  TIPOS_PERMITIDOS,
  crearImagenLocal,
  filtrarArchivos,
  liberarPreview,
  mover,
} from '../utils/imagenes'
import { Button } from './ui'

const ETIQUETA_ESTADO = {
  subiendo: { texto: 'Subiendo...', clase: 'bg-rio-600' },
  subida: { texto: 'Subida', clase: 'bg-green-600' },
  error: { texto: 'Falló', clase: 'bg-red-600' },
}

function BotonIcono({ label, onClick, disabled, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      aria-label={label}
      title={label}
      className="flex h-7 w-7 items-center justify-center rounded-full bg-white/90 text-gray-700 shadow hover:bg-white disabled:opacity-40 focus:outline-none focus-visible:ring-2 focus-visible:ring-rio-500"
    >
      {children}
    </button>
  )
}

function Icono({ d }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4">
      <path strokeLinecap="round" strokeLinejoin="round" d={d} />
    </svg>
  )
}

/**
 * Elegir imágenes para una propiedad (#77): vista previa, orden y quitar antes de subir.
 * La primera es la portada. El orden se cambia arrastrando o con las flechas.
 *
 * @param {Array}    props.imagenes    [{ id, archivo, preview }] en el orden en que se van a subir
 * @param {Function} props.onChange    recibe la lista nueva
 * @param {object}   [props.estados]   { [id]: 'subiendo' | 'subida' | 'error' } mientras se suben
 * @param {boolean}  [props.bloqueado] true cuando ya se empezó a subir: no se puede editar la lista
 * @param {number}   [props.yaSubidas=0] al editar: fotos que la propiedad ya tiene. Cuentan para el
 *                   máximo y las nuevas van después de ellas (no son la portada)
 */
export default function SelectorImagenes({ imagenes, onChange, estados = {}, bloqueado = false, yaSubidas = 0 }) {
  const inputRef = useRef(null)
  const [errores, setErrores] = useState([])
  const [arrastrando, setArrastrando] = useState(null) // índice de la miniatura que se arrastra
  const [sobreZona, setSobreZona] = useState(false) // archivos arrastrados sobre la zona

  const agregar = (archivos) => {
    const { validos, errores: encontrados } = filtrarArchivos(Array.from(archivos), yaSubidas + imagenes.length)
    setErrores(encontrados)
    if (validos.length > 0) onChange([...imagenes, ...validos.map(crearImagenLocal)])
  }

  const quitar = (indice) => {
    liberarPreview(imagenes[indice])
    onChange(imagenes.filter((_, i) => i !== indice))
    setErrores([])
  }

  const moverA = (desde, hasta) => onChange(mover(imagenes, desde, hasta))

  const elegirArchivos = (event) => {
    agregar(event.target.files)
    event.target.value = '' // permite volver a elegir el mismo archivo después de quitarlo
  }

  // Arrastrar archivos desde la compu a la zona
  const soltarEnZona = (event) => {
    event.preventDefault()
    setSobreZona(false)
    if (!bloqueado && event.dataTransfer.files.length > 0) agregar(event.dataTransfer.files)
  }

  // Arrastrar una miniatura sobre otra para reordenar
  const soltarEnMiniatura = (indice) => (event) => {
    event.preventDefault()
    event.stopPropagation()
    if (arrastrando !== null) moverA(arrastrando, indice)
    setArrastrando(null)
  }

  const total = yaSubidas + imagenes.length
  const lleno = total >= MAXIMO_IMAGENES

  return (
    <div>
      {!bloqueado && (
        <div
          onDragOver={(event) => {
            event.preventDefault()
            if (arrastrando === null) setSobreZona(true)
          }}
          onDragLeave={() => setSobreZona(false)}
          onDrop={soltarEnZona}
          className={`flex flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed px-4 py-8 text-center ${
            sobreZona ? 'border-rio-500 bg-rio-50' : 'border-gray-300'
          }`}
        >
          <p className="text-sm text-gray-600">Arrastrá las fotos acá o</p>
          <Button variante="secundario" tamanio="sm" onClick={() => inputRef.current?.click()} disabled={lleno}>
            Elegir fotos
          </Button>
          <p className="text-xs text-gray-400">
            JPG, PNG o WEBP · hasta {TAMANIO_MAXIMO_MB} MB cada una · máximo {MAXIMO_IMAGENES} ({total}/
            {MAXIMO_IMAGENES})
          </p>
          <input
            ref={inputRef}
            id="imagenes"
            type="file"
            accept={TIPOS_PERMITIDOS.join(',')}
            multiple
            onChange={elegirArchivos}
            className="sr-only"
            tabIndex={-1}
          />
        </div>
      )}

      {!bloqueado && errores.length > 0 && (
        <ul role="alert" className="mt-2 space-y-1 text-sm text-red-600">
          {errores.map((error) => (
            <li key={error}>{error}</li>
          ))}
        </ul>
      )}

      {imagenes.length > 0 && (
        <>
          {!bloqueado && imagenes.length > 1 && (
            <p className="mt-3 text-xs text-gray-500">
              {yaSubidas === 0 && 'La primera es la portada. '}
              Arrastrá las fotos o usá las flechas para cambiar el orden.
            </p>
          )}
          <ol className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3">
            {imagenes.map((imagen, indice) => {
              const estado = ETIQUETA_ESTADO[estados[imagen.id]]
              return (
                <li
                  key={imagen.id}
                  draggable={!bloqueado}
                  onDragStart={() => setArrastrando(indice)}
                  onDragEnd={() => setArrastrando(null)}
                  onDragOver={(event) => event.preventDefault()}
                  onDrop={soltarEnMiniatura(indice)}
                  className={`group relative aspect-[4/3] overflow-hidden rounded-xl border bg-gray-100 ${
                    arrastrando === indice ? 'opacity-40' : ''
                  } ${bloqueado ? '' : 'cursor-move'} ${
                    yaSubidas + indice === 0 ? 'border-rio-500 ring-2 ring-rio-200' : 'border-gray-200'
                  }`}
                >
                  <img src={imagen.preview} alt={`Foto ${yaSubidas + indice + 1}: ${imagen.archivo.name}`} className="h-full w-full object-cover" />

                  <span className="absolute left-2 top-2 rounded bg-black/60 px-1.5 py-0.5 text-xs font-bold text-white">
                    {yaSubidas + indice === 0 ? 'Portada' : yaSubidas + indice + 1}
                  </span>

                  {estado && (
                    <span className={`absolute right-2 top-2 rounded px-1.5 py-0.5 text-xs font-bold text-white ${estado.clase}`}>
                      {estado.texto}
                    </span>
                  )}

                  {!bloqueado && (
                    <div className="absolute inset-x-0 bottom-0 flex justify-between bg-gradient-to-t from-black/50 to-transparent p-2">
                      <div className="flex gap-1">
                        <BotonIcono label="Mover antes" onClick={() => moverA(indice, indice - 1)} disabled={indice === 0}>
                          <Icono d="m15 18-6-6 6-6" />
                        </BotonIcono>
                        <BotonIcono
                          label="Mover después"
                          onClick={() => moverA(indice, indice + 1)}
                          disabled={indice === imagenes.length - 1}
                        >
                          <Icono d="m9 18 6-6-6-6" />
                        </BotonIcono>
                      </div>
                      <BotonIcono label="Quitar foto" onClick={() => quitar(indice)}>
                        <Icono d="M6 18 18 6M6 6l12 12" />
                      </BotonIcono>
                    </div>
                  )}
                </li>
              )
            })}
          </ol>
        </>
      )}
    </div>
  )
}
