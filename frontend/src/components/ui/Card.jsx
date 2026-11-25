import { CLASES_CARD } from './clases'

const PADDINGS = {
  ninguno: '',
  sm: 'p-4',
  md: 'p-6',
}


/**
 * Contenedor con borde, fondo blanco y sombra (#87): formularios, tarjetas de los listados, cajas.
 *
 * @param {string|Component} [props.as='div']  etiqueta o componente a renderizar (li, section, form, fieldset, Link...)
 * @param {'ninguno'|'sm'|'md'} [props.padding='md']
 */
export default function Card({ as: Etiqueta = 'div', padding = 'md', className = '', children, ...props }) {
  return (
    <Etiqueta className={`${CLASES_CARD} ${PADDINGS[padding]} ${className}`.trim()} {...props}>
      {children}
    </Etiqueta>
  )
}
