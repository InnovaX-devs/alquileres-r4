import { useState } from 'react'
import CampoFormulario from './CampoFormulario'
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
      <CampoFormulario
        id="nombre"
        label="Nombre"
        autoComplete="name"
        value={datos.nombre}
        onChange={cambiar('nombre')}
        error={errores.nombre}
      />
      <CampoFormulario
        id="telefono"
        label="Teléfono"
        type="tel"
        autoComplete="tel"
        placeholder="Ej: +54 9 11 1234-5678"
        value={datos.telefono}
        onChange={cambiar('telefono')}
        error={errores.telefono}
      />
      <CampoFormulario
        id="documento"
        label="Documento"
        placeholder="DNI o pasaporte"
        value={datos.documento}
        onChange={cambiar('documento')}
        error={errores.documento}
      />
      <p className="-mt-2 text-xs text-gray-500">
        Completá {DATOS_PARA_VERIFICAR} para que tu perfil quede verificado.
      </p>

      <div>
        <label htmlFor="descripcionPerfil" className="block text-sm font-medium text-gray-700">
          Descripción <span className="font-normal text-gray-400">(opcional)</span>
        </label>
        <textarea
          id="descripcionPerfil"
          name="descripcionPerfil"
          rows={4}
          placeholder="Contá algo sobre vos"
          value={datos.descripcionPerfil}
          onChange={cambiar('descripcionPerfil')}
          aria-invalid={!!errores.descripcionPerfil}
          aria-describedby={errores.descripcionPerfil ? 'descripcionPerfil-error' : undefined}
          className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 ${
            errores.descripcionPerfil
              ? 'border-red-400 focus:ring-red-200'
              : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200'
          }`}
        />
        <div className="mt-1 flex justify-between text-xs">
          <span id="descripcionPerfil-error" className="text-red-600">
            {errores.descripcionPerfil}
          </span>
          <span className="text-gray-400">
            {datos.descripcionPerfil.trim().length}/{DESCRIPCION_MAXIMA}
          </span>
        </div>
      </div>

      {errorGeneral && (
        <p role="alert" className="text-sm text-red-600">
          {errorGeneral}
        </p>
      )}

      <div className="flex justify-end gap-2">
        <button
          type="button"
          onClick={onCancelar}
          disabled={enviando}
          className="px-4 py-2 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-100 disabled:opacity-60"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={enviando}
          className="px-4 py-2 rounded-md bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-60"
        >
          {enviando ? 'Guardando...' : 'Guardar cambios'}
        </button>
      </div>
    </form>
  )
}
