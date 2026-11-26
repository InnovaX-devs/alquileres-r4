/**
 * Formas de fondo de las pantallas de acceso: a la izquierda la casita de línea del hero, gigante y cortada,
 * y a la derecha pinceladas en el petróleo del hero. Son decorativas, quedan
 * fijas detrás del contenido (no agregan scroll) y a los costados para no tapar el título.
 */
function FormasDeFondo() {
  return (
    <div aria-hidden="true" className="pointer-events-none fixed inset-0 -z-10 overflow-hidden">
      {/* A la izquierda, la casita de línea del hero del inicio en grande, cortada por el borde de la pantalla */}
      <svg
        viewBox="0 0 100 100"
        fill="none"
        className="absolute -bottom-16 -left-28 w-80 md:bottom-auto md:-left-[200px] md:top-[20%] md:w-[460px] lg:-left-[250px] lg:w-[560px]"
      >
        <path d="M22 80V45L50 22l28 23v35" className="stroke-pino-800" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" />
      </svg>

      {/* Pincelada alargada a la derecha */}
      <div className="absolute -right-24 top-[24%] hidden h-[480px] w-[230px] rotate-[18deg] rounded-[50%_50%_46%_54%/36%_40%_60%_64%] bg-pino-800 md:block" />
      {/* Mancha chica, más suave, arriba de la pincelada */}
      <div className="absolute right-[150px] top-[18%] hidden h-24 w-32 rotate-[-14deg] rounded-[63%_37%_54%_46%/55%_48%_52%_45%] bg-rio-200 md:block" />
    </div>
  )
}

/**
 * Marco de las pantallas de ingreso y registro: título y formulario centrados, con formas de fondo.
 *
 * @param {string} props.titulo  el h1 de la página
 */
export default function PantallaAcceso({ titulo, children }) {
  return (
    // Centrado vertical en el alto que queda libre: pantalla menos barra (84px), márgenes del main (64px)
    // y footer (~96px, con margen por si en celulares ocupa dos líneas). Si el contenido es más alto
    // (registro en pantallas bajas) no fuerza nada: crece normalmente.
    <div className="mx-auto flex min-h-[calc(100dvh-244px)] max-w-lg flex-col justify-center">
      <FormasDeFondo />
      <h1 className="text-center text-[2.25rem] font-bold leading-tight tracking-[-0.03em]">{titulo}</h1>
      <div className="mt-5">{children}</div>
    </div>
  )
}
