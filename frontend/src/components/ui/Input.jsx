// Campos de formulario comunes (#87): Input, Select y Textarea con label, error, ayuda y contador.
// Reemplazan a CampoFormulario y a los selects/textareas armados a mano en cada formulario.

import { clasesControl } from './clases'

/** Ids para conectar el control con su error y su ayuda (lectores de pantalla). */
function idsDescripcion(id, { error, ayuda }) {
  const ids = [error && `${id}-error`, ayuda && `${id}-ayuda`].filter(Boolean)
  return ids.length > 0 ? ids.join(' ') : undefined
}

/**
 * Label + control + mensajes. Lo usan Input, Select y Textarea; también sirve para envolver
 * un control propio.
 *
 * @param {string}  props.id
 * @param {node}    props.label
 * @param {boolean} [props.opcional]  agrega "(opcional)" al label
 * @param {string}  [props.error]
 * @param {node}    [props.ayuda]     texto de ayuda debajo del campo
 * @param {{ actual: number, maximo: number }} [props.contador]  "12/1000" a la derecha
 */
export function Campo({ id, label, opcional, error, ayuda, contador, className = '', children }) {
  return (
    <div className={className}>
      {label && (
        <label htmlFor={id} className="block text-sm font-bold text-gray-700">
          {label} {opcional && <span className="font-normal text-gray-400">(opcional)</span>}
        </label>
      )}
      {children}
      {(error || ayuda || contador) && (
        <div className="mt-1 flex justify-between gap-4 text-sm">
          <div>
            {error && (
              <p id={`${id}-error`} className="text-red-600">
                {error}
              </p>
            )}
            {ayuda && (
              <p id={`${id}-ayuda`} className="text-xs text-gray-500">
                {ayuda}
              </p>
            )}
          </div>
          {contador && (
            <span
              className={`shrink-0 text-xs ${contador.actual > contador.maximo ? 'text-red-600' : 'text-gray-400'}`}
            >
              {contador.actual}/{contador.maximo}
            </span>
          )}
        </div>
      )}
    </div>
  )
}

// Props del control que se comparten entre Input, Select y Textarea
function propsControl({ id, error, ayuda, className }) {
  return {
    id,
    name: id,
    'aria-invalid': !!error || undefined,
    'aria-describedby': idsDescripcion(id, { error, ayuda }),
    className: clasesControl(error, className),
  }
}

/**
 * Input con label y error.
 * Ej: <Input id="email" label="Email" type="email" value={email} onChange={...} error={errores.email} />
 */
export default function Input({ id, label, opcional, error, ayuda, contador, className, ...props }) {
  return (
    <Campo id={id} label={label} opcional={opcional} error={error} ayuda={ayuda} contador={contador}>
      <input {...propsControl({ id, error, ayuda, className })} {...props} />
    </Campo>
  )
}

/**
 * Select con label y error. Las opciones van como `opciones` ([{ valor, label }]) o como children.
 */
export function Select({ id, label, opcional, error, ayuda, opciones, className, children, ...props }) {
  return (
    <Campo id={id} label={label} opcional={opcional} error={error} ayuda={ayuda}>
      <select {...propsControl({ id, error, ayuda, className })} {...props}>
        {opciones
          ? opciones.map(({ valor, label: texto }) => (
              <option key={valor} value={valor}>
                {texto}
              </option>
            ))
          : children}
      </select>
    </Campo>
  )
}

/** Textarea con label, error y contador opcional. */
export function Textarea({ id, label, opcional, error, ayuda, contador, rows = 4, className, ...props }) {
  return (
    <Campo id={id} label={label} opcional={opcional} error={error} ayuda={ayuda} contador={contador}>
      <textarea rows={rows} {...propsControl({ id, error, ayuda, className })} {...props} />
    </Campo>
  )
}
