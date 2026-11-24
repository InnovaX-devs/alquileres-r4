import { useState } from 'react'

function SinFotos() {
  return (
    <div className="flex aspect-[16/10] w-full flex-col items-center justify-center gap-2 rounded-lg bg-gray-100 text-gray-400">
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-14 w-14">
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1v-9.5Z" />
      </svg>
      <span className="text-sm">Esta propiedad no tiene fotos</span>
    </div>
  )
}

function Flecha({ direccion, onClick, label }) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={label}
      className={`absolute top-1/2 -translate-y-1/2 ${direccion === 'izq' ? 'left-3' : 'right-3'} flex h-10 w-10 items-center justify-center rounded-full bg-white/80 text-gray-800 shadow hover:bg-white focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500`}
    >
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5">
        <path strokeLinecap="round" strokeLinejoin="round" d={direccion === 'izq' ? 'm15 18-6-6 6-6' : 'm9 18 6-6-6-6'} />
      </svg>
    </button>
  )
}

/**
 * Galería del detalle de propiedad (#74): foto principal con flechas y miniaturas.
 * Las imágenes llegan ordenadas desde el backend. Las que no cargan se ocultan.
 *
 * @param {string[]} props.imagenes  URLs
 * @param {string}   props.titulo    para el texto alternativo
 */
export default function GaleriaImagenes({ imagenes = [], titulo }) {
  const [rotas, setRotas] = useState(() => new Set())
  const [actual, setActual] = useState(0)

  const visibles = imagenes.filter((url) => !rotas.has(url))
  if (visibles.length === 0) return <SinFotos />

  const indice = Math.min(actual, visibles.length - 1)
  const marcarRota = (url) => setRotas((anteriores) => new Set(anteriores).add(url))
  const mover = (paso) => setActual((indice + paso + visibles.length) % visibles.length)
  const descripcion = titulo || 'la propiedad'

  return (
    <div>
      <div
        className="relative aspect-[16/10] w-full overflow-hidden rounded-lg bg-gray-100"
        onKeyDown={(e) => {
          if (e.key === 'ArrowLeft') mover(-1)
          if (e.key === 'ArrowRight') mover(1)
        }}
      >
        <img
          key={visibles[indice]}
          src={visibles[indice]}
          alt={`Foto ${indice + 1} de ${visibles.length} de ${descripcion}`}
          onError={() => marcarRota(visibles[indice])}
          className="h-full w-full object-cover"
        />
        {visibles.length > 1 && (
          <>
            <Flecha direccion="izq" onClick={() => mover(-1)} label="Foto anterior" />
            <Flecha direccion="der" onClick={() => mover(1)} label="Foto siguiente" />
            <span className="absolute bottom-3 right-3 rounded-full bg-black/60 px-2 py-0.5 text-xs text-white">
              {indice + 1} / {visibles.length}
            </span>
          </>
        )}
      </div>

      {visibles.length > 1 && (
        <ul className="mt-3 flex gap-2 overflow-x-auto pb-1">
          {visibles.map((url, i) => (
            <li key={url} className="shrink-0">
              <button
                type="button"
                onClick={() => setActual(i)}
                aria-label={`Ver foto ${i + 1}`}
                aria-current={i === indice}
                className={`block h-16 w-24 overflow-hidden rounded-md border-2 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 ${
                  i === indice ? 'border-blue-600' : 'border-transparent opacity-70 hover:opacity-100'
                }`}
              >
                <img src={url} alt="" loading="lazy" onError={() => marcarRota(url)} className="h-full w-full object-cover" />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
