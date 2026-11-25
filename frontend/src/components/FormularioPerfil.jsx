import { useState } from 'react'
import { Aviso, Button, Input, Textarea } from './ui'
import { CAMPOS_PARA_VERIFICAR, unirConY } from '../utils/perfil'

// Las columnas nombre, telefono y documento son VARCHAR(255): más largo, el backend falla con un 500
const LARGO_MAXIMO = 255
const DESCRIPCION_MAXIMA = 1000
// Dígitos, espacios, +, -, paréntesis. Ej: +54 9 11 1234-5678
const TELEFONO_REGEX = /^\+?[\d\s()-]+$/
const TELEFONO_MIN_DIGITOS = 6

// "nombre, teléfono y documento"
const DATOS_PARA_VERIFICAR = unirConY(CAMPOS_PARA_VERIFICAR.map(({ label }) => label.toLowerCase()))

function validar({ nombre, telefono, documento, descripcionPerfil }) {
  const errores = {}
  const tel = telefono.trim()

  if (!nombre.trim()) errores.nombre = 'Ingresá tu nombre'
  else if (nombre.trim().length > LARGO_MAXIMO) errores.nombre = `Máximo ${LARGO_MAXIMO} caracteres`

  if (tel) {
    if (!TELEFONO_REGEX.test(tel)) {
      errores.telefono = 'Usá solo números, espacios, +, - y paréntesis'
    } else if (tel.replace(/\D/g, '').length < TELEFONO_MIN_DIGITOS) {
      errores.telefono = `El teléfono debe tener al menos ${TELEFONO_MIN_DIGITOS} números`
    } else if (tel.length > LARGO_MAXIMO) {
      errores.telefono = `Máximo ${LARGO_MAXIMO} caracteres`
    }
  }

  if (documento.trim().length > LARGO_MAXIMO) errores.documento = `Máximo ${LARGO_MAXIMO} caracteres`

  if (descripcionPerfil.trim().length > DESCRIPCION_MAXIMA) {
    errores.descripcionPerfil = `Máximo ${DESCRIPCION_MAXIMA} caracteres`
  }

  return errores
}

function valorInicial(valor) {
  return valor ?? ''
}

/**
 * Formulario de completar perfil (#71). Consume PATCH /usuarios/perfil a través de `onGuardar`.
 *
 * @param {object}   props.usuario    usuario actual (GET /usuarios/me)
 * @param {Function} props.onGuardar  recibe solo los campos que cambiaron; debe lanzar ApiError si falla
 * @param {Function} props.onCancelar
 */
export default function FormularioPerfil({ usuario, onGuardar, onCancelar }) {
  const [datos, setDatos] = useState(() => ({
    nombre: valorInicial(usuario.nombre),
    telefono: valorInicial(usuario.telefono),
    documento: valorInicial(usuario.documento),
    descripcionPerfil: valorInicial(usuario.descripcionPerfil),
  }))
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo) => (event) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: event.target.value }))
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined }))
  }

  const enviar = async (event) => {
    event.preventDefault()
    setErrorGeneral(null)

    const encontrados = validar(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    // Solo se mandan los campos que cambiaron. Un campo vaciado se manda como "" para que el
    // backend lo borre (si se manda null, el backend lo ignora y deja el valor anterior).
    const cambios = {}
    for (const [campo, valor] of Object.entries(datos)) {
      const limpio = valor.trim()
      if (limpio !== valorInicial(usuario[campo]).trim()) cambios[campo] = limpio
    }

    if (Object.keys(cambios).length === 0) {
      onCancelar()
      return
    }

    setEnviando(true)
    try {
      await onGuardar(cambios)
      // Si salió bien, la pantalla de perfil cierra el formulario
    } catch (e) {
      setErrorGeneral(e.detalles?.join('. ') || e.mensaje || 'No se pudo guardar el perfil')
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={enviar} noValidate className="mt-4 space-y-4">
      <Input
        id="nombre"
        label="Nombre"
        autoComplete="name"
        value={datos.nombre}
        onChange={cambiar('nombre')}
        error={errores.nombre}
      />
      <Input
        id="telefono"
        label="Teléfono"
        type="tel"
        autoComplete="tel"
        placeholder="Ej: +54 9 11 1234-5678"
        value={datos.telefono}
        onChange={cambiar('telefono')}
        error={errores.telefono}
      />
      <Input
        id="documento"
        label="Documento"
        placeholder="DNI o pasaporte"
        value={datos.documento}
        onChange={cambiar('documento')}
        error={errores.documento}
        ayuda={`Completá ${DATOS_PARA_VERIFICAR} para que tu perfil quede verificado.`}
      />

      <Textarea
        id="descripcionPerfil"
        label="Descripción"
        opcional
        placeholder="Contá algo sobre vos"
        value={datos.descripcionPerfil}
        onChange={cambiar('descripcionPerfil')}
        error={errores.descripcionPerfil}
        contador={{ actual: datos.descripcionPerfil.trim().length, maximo: DESCRIPCION_MAXIMA }}
      />

      {errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}

      <div className="flex justify-end gap-2">
        <Button variante="fantasma" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </Button>
        <Button type="submit" cargando={enviando}>
          {enviando ? 'Guardando...' : 'Guardar cambios'}
        </Button>
      </div>
    </form>
  )
}
