import { Link } from 'react-router-dom'
import Spinner from './Spinner'
import { clasesBoton } from './clases'

/**
 * Botón común (#87). Con `to` se renderiza como Link de react-router (mismo aspecto).
 *
 * @param {'primario'|'oscuro'|'secundario'|'fantasma'|'suave'|'exito'|'advertencia'|'peligro'|'peligroSuave'} [props.variante='primario']
 *        primario (amarillo) es la acción principal de la pantalla: usar uno por pantalla.
 * @param {'sm'|'md'|'lg'} [props.tamanio='md']
 * @param {boolean} [props.anchoCompleto]  ocupa todo el ancho
 * @param {boolean} [props.cargando]       deshabilita el botón y muestra un spinner antes del texto
 * @param {string}  [props.to]             ruta interna: se renderiza un <Link>
 *
 * El `type` por defecto es "button" (el del HTML es "submit"); para enviar un formulario, pasar type="submit".
 */
export default function Button({
  variante = 'primario',
  tamanio = 'md',
  anchoCompleto = false,
  cargando = false,
  to,
  type = 'button',
  disabled,
  className = '',
  children,
  ...props
}) {
  const clases = clasesBoton({ variante, tamanio, anchoCompleto, className })

  if (to) {
    return (
      <Link to={to} className={clases} {...props}>
        {children}
      </Link>
    )
  }

  return (
    <button type={type} disabled={disabled || cargando} aria-busy={cargando || undefined} className={clases} {...props}>
      {cargando && <Spinner tamanio="sm" />}
      {children}
    </button>
  )
}
