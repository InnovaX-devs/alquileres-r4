import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import SelectorImagenes from '../components/SelectorImagenes'
import { Aviso, Button, Card, Input, Select, Textarea } from '../components/ui'
import { publicarPropiedad, subirImagenPropiedad } from '../services/propiedades'
import { liberarPreview } from '../utils/imagenes'
import {
  MONEDAS,
  OPCIONES_IMPUESTOS,
  PROPIEDAD_VACIA,
  armarPropiedadRequest,
  erroresDelBackend,
  validarPropiedad,
} from '../utils/formularioPropiedad'

// PROPIEDAD_VACIA tiene los campos en el orden de la pantalla: al haber errores se enfoca el primero
const ORDEN_CAMPOS = Object.keys(PROPIEDAD_VACIA)

function Seccion({ titulo, children }) {
  return (
    <Card as="fieldset" className="space-y-4">
      <legend className="px-1 text-base font-semibold text-gray-900">{titulo}</legend>
      {children}
    </Card>
  )
}

function enfocarPrimerError(errores) {
  const campo = ORDEN_CAMPOS.find((clave) => errores[clave])
  if (campo) document.getElementById(campo)?.focus()
}

/**
 * Formulario de publicar propiedad (#76) con subida de imágenes (#77).
 *
 * 1. POST /propiedades crea la propiedad.
 * 2. Las fotos se suben de a una con POST /propiedades/{id}/imagenes, en el orden elegido
 *    (el backend asigna el orden según llegan).
 * 3. Si todo sale bien, redirige al detalle.
 *
 * Si falla una foto se corta ahí, para no desordenar las siguientes: la propiedad ya quedó creada,
 * así que el formulario se bloquea y se puede reintentar desde esa foto o ir igual al detalle.
 */
export default function PublicarPropiedad() {
  const navigate = useNavigate()

  const [datos, setDatos] = useState(PROPIEDAD_VACIA)
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const [imagenes, setImagenes] = useState([])
  const [estadosImagenes, setEstadosImagenes] = useState({}) // { [id]: 'subiendo' | 'subida' | 'error' }
  const [propiedadId, setPropiedadId] = useState(null) // ya creada: falta terminar de subir fotos
  const [errorImagenes, setErrorImagenes] = useState(null)

  // Al salir de la pantalla se liberan las vistas previas (URLs locales de los archivos)
  const imagenesRef = useRef(imagenes)
  useEffect(() => {
    imagenesRef.current = imagenes
  }, [imagenes])
  useEffect(() => () => imagenesRef.current.forEach(liberarPreview), [])

  const irAlDetalle = (id, imagenesSinSubir = 0) =>
    navigate(`/propiedades/${id}`, { replace: true, state: { publicada: true, imagenesSinSubir } })

  /** Sube en orden las fotos que faltan. Devuelve true si se subieron todas. */
  const subirImagenes = async (id) => {
    const estados = { ...estadosImagenes }
    for (const imagen of imagenes) {
      if (estados[imagen.id] === 'subida') continue
      estados[imagen.id] = 'subiendo'
      setEstadosImagenes({ ...estados })
      try {
        await subirImagenPropiedad(id, imagen.archivo)
        estados[imagen.id] = 'subida'
        setEstadosImagenes({ ...estados })
      } catch (e) {
        estados[imagen.id] = 'error'
        setEstadosImagenes({ ...estados })
        setErrorImagenes(`No se pudo subir ${imagen.archivo.name}: ${e.mensaje || 'error desconocido'}`)
        return false
      }
    }
    return true
  }

  const imagenesSinSubir = imagenes.filter((imagen) => estadosImagenes[imagen.id] !== 'subida').length

  const cambiar = (campo) => (event) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: event.target.value }))
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined }))
  }

  // Props comunes de cada campo
  const campo = (clave) => ({ id: clave, value: datos[clave], onChange: cambiar(clave), error: errores[clave] })

  const enviar = async (event) => {
    event.preventDefault()
    setErrorGeneral(null)
    setErrorImagenes(null)

    // La propiedad ya existe: solo se reintenta la subida de las fotos que faltan
    if (propiedadId) {
      setEnviando(true)
      if (await subirImagenes(propiedadId)) irAlDetalle(propiedadId)
      else setEnviando(false)
      return
    }

    const encontrados = validarPropiedad(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) {
      enfocarPrimerError(encontrados)
      return
    }

    setEnviando(true)
    let creada
    try {
      creada = await publicarPropiedad(armarPropiedadRequest(datos))
    } catch (e) {
      // 401: el cliente HTTP ya cierra la sesión y manda al login (SesionExpirada.jsx)
      const { porCampo, otros } = erroresDelBackend(e.detalles)
      setErrores(porCampo)
      enfocarPrimerError(porCampo)
      if (Object.keys(porCampo).length === 0 || otros.length > 0) {
        setErrorGeneral(otros.length > 0 ? otros.join('. ') : e.mensaje || 'No se pudo publicar la propiedad')
      }
      setEnviando(false)
      return
    }

    setPropiedadId(creada.id)
    if (await subirImagenes(creada.id)) irAlDetalle(creada.id)
    else setEnviando(false)
  }

  const textoBoton = () => {
    if (!enviando) return propiedadId ? 'Reintentar subida' : 'Publicar propiedad'
    if (!propiedadId) return 'Publicando...'
    const subidas = imagenes.length - imagenesSinSubir
    return `Subiendo fotos (${Math.min(subidas + 1, imagenes.length)}/${imagenes.length})...`
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-2xl font-bold">Publicar propiedad</h1>
      <p className="mt-1 text-sm text-gray-600">
        Los campos marcados como opcionales se pueden dejar vacíos.
      </p>

      <form onSubmit={enviar} noValidate className="mt-6 space-y-6">
        {/* Con la propiedad ya creada, sus datos no se pueden cambiar desde acá */}
        <fieldset disabled={enviando || propiedadId !== null} className="space-y-6">
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
        </fieldset>

        <Seccion titulo="Fotos">
          <SelectorImagenes
            imagenes={imagenes}
            onChange={setImagenes}
            estados={estadosImagenes}
            bloqueado={enviando || propiedadId !== null}
          />
        </Seccion>

        {errorImagenes && (
          <Aviso tipo="advertencia" titulo="La propiedad se publicó, pero no se pudieron subir todas las fotos.">
            <p>{errorImagenes}</p>
            <p className="mt-1">
              Podés reintentar desde esa foto o ir a la propiedad sin las {imagenesSinSubir} que faltan.
            </p>
          </Aviso>
        )}

        {errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}

        <div className="flex justify-end gap-2">
          {propiedadId ? (
            <Button variante="fantasma" onClick={() => irAlDetalle(propiedadId, imagenesSinSubir)} disabled={enviando}>
              Ir a la propiedad
            </Button>
          ) : (
            <Button variante="fantasma" to="/">
              Cancelar
            </Button>
          )}
          <Button type="submit" cargando={enviando}>
            {textoBoton()}
          </Button>
        </div>
      </form>
    </div>
  )
}
