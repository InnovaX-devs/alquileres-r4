import { Card, Input, Select, Textarea } from './ui'
import SelectorUbicacion from './SelectorUbicacion'
import { MONEDAS, OPCIONES_IMPUESTOS } from '../utils/formularioPropiedad'

export function Seccion({ titulo, children }) {
  return (
    <Card as="fieldset">
      {/* float + clear: así el legend queda adentro de la tarjeta y no cortando el borde */}
      <legend className="float-left w-full font-display text-xl font-bold tracking-tight text-gray-900">{titulo}</legend>
      <div className="clear-both space-y-4 pt-4">{children}</div>
    </Card>
  )
}

/**
 * Campos del formulario de una propiedad (todo menos las fotos). Lo usan publicar (#76) y editar.
 *
 * @param {object}   props.datos        valores del formulario (ver PROPIEDAD_VACIA)
 * @param {object}   props.errores      { campo: mensaje }
 * @param {Function} props.onCambiar    recibe (campo, valor)
 * @param {{latitud, longitud} | null} props.ubicacion
 * @param {Function} props.onCambiarUbicacion
 * @param {boolean}  [props.ubicacionGuardada]  al editar: la ubicación ya la confirmó quien publicó
 */
export default function CamposPropiedad({ datos, errores, onCambiar, ubicacion, onCambiarUbicacion, ubicacionGuardada = false }) {
  // Props comunes de cada campo
  const campo = (clave) => ({
    id: clave,
    value: datos[clave],
    onChange: (event) => onCambiar(clave, event.target.value),
    error: errores[clave],
  })

  return (
    <>
      <Seccion titulo="Datos principales">
        <Input label="Título" placeholder="Ej: Departamento 2 ambientes con balcón" {...campo('titulo')} />
        <Textarea
          label="Descripción"
          opcional
          placeholder="Contá cómo es la propiedad, qué tiene cerca, estado, etc."
          {...campo('descripcion')}
        />
      </Seccion>

      <Seccion titulo="Precio">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            label="Precio mensual"
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            placeholder="Ej: 350000"
            {...campo('precio')}
          />
          <Select label="Moneda" opciones={MONEDAS} {...campo('moneda')} />
          <Input
            label="Expensas"
            opcional
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            placeholder="En la misma moneda del precio"
            {...campo('expensas')}
          />
          <Select
            label="¿El precio incluye impuestos?"
            opcional
            opciones={OPCIONES_IMPUESTOS}
            {...campo('incluyeImpuestos')}
          />
          <Input
            label="Actualización del precio (meses)"
            opcional
            type="number"
            inputMode="numeric"
            min="1"
            step="1"
            placeholder="Ej: 3"
            {...campo('plazoActualizacionMeses')}
          />
        </div>
      </Seccion>

      <Seccion titulo="Ubicación">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="sm:col-span-2">
            <Input label="Calle" autoComplete="address-line1" placeholder="Ej: Sobremonte" {...campo('calle')} />
          </div>
          <Input label="Altura" placeholder="Ej: 1234" {...campo('altura')} />
          <Input label="Piso / depto" opcional placeholder="Ej: 5B" {...campo('piso')} />
        </div>
        <SelectorUbicacion
          calle={datos.calle}
          altura={datos.altura}
          ubicacion={ubicacion}
          onCambiar={onCambiarUbicacion}
          guardada={ubicacionGuardada}
        />
      </Seccion>

      <Seccion titulo="Características">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            label="Habitaciones"
            opcional
            type="number"
            inputMode="numeric"
            min="0"
            step="1"
            placeholder="0 = monoambiente"
            {...campo('habitaciones')}
          />
          <Input
            label="Metros cuadrados"
            opcional
            type="number"
            inputMode="numeric"
            min="1"
            step="1"
            placeholder="Ej: 45"
            {...campo('metrosCuadrados')}
          />
        </div>
        <Textarea
          label="Requisitos de ingreso"
          opcional
          placeholder="Ej: garantía propietaria, recibo de sueldo, depósito"
          {...campo('requisitosIngreso')}
        />
      </Seccion>
    </>
  )
}
