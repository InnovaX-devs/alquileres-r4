import { Button } from './ui'
import { linkParaCompartir, linkWhatsApp, textoParaCompartir, usarHojaDelSistema } from '../utils/compartir'

function IconoCompartir({ className }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className={className}>
      <circle cx="18" cy="5" r="2.6" />
      <circle cx="6" cy="12" r="2.6" />
      <circle cx="18" cy="19" r="2.6" />
      <path d="m8.3 10.8 7.4-4.4M8.3 13.2l7.4 4.4" />
    </svg>
  )
}

/**
 * Botón "Compartir" de una propiedad (#151).
 * En el celular abre la hoja de compartir del sistema; en la compu, WhatsApp (wa.me) con el título,
 * el precio y el link. El link es el del back, que le da a WhatsApp la vista previa con foto.
 *
 * @param {object} props.propiedad  necesita id, titulo, precio, moneda y la dirección
 * @param {'icono'|'boton'} [props.variante='boton']  icono: redondo, para ir sobre la foto de la tarjeta
 * @param {string} [props.className]
 */
export default function BotonCompartir({ propiedad, variante = 'boton', className = '' }) {
  const compartir = async () => {
    if (usarHojaDelSistema()) {
      try {
        await navigator.share({
          title: propiedad.titulo?.trim() || 'Propiedad en Alkilea',
          text: textoParaCompartir(propiedad),
          url: linkParaCompartir(propiedad.id),
        })
        return
      } catch (e) {
        // La persona cerró la hoja sin elegir nada: no es un error
        if (e?.name === 'AbortError') return
        // Si la hoja falla por otra cosa, se sigue con WhatsApp
      }
    }
    window.open(linkWhatsApp(propiedad), '_blank', 'noopener,noreferrer')
  }

  if (variante === 'icono') {
    return (
      <button
        type="button"
        onClick={compartir}
        aria-label="Compartir propiedad"
        title="Compartir"
        className={`inline-flex h-11 w-11 items-center justify-center rounded-full bg-white/90 text-gray-900 transition hover:scale-105 focus:outline-none focus-visible:ring-3 focus-visible:ring-rio-600 active:scale-95 ${className}`}
      >
        <IconoCompartir className="h-[20px] w-[20px]" />
      </button>
    )
  }

  return (
    <Button variante="secundario" onClick={compartir} className={className}>
      <IconoCompartir className="h-[18px] w-[18px]" />
      Compartir
    </Button>
  )
}
