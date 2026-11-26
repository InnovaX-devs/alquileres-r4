import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import CamposPropiedad, { Seccion } from '../components/CamposPropiedad'
import SelectorImagenes from '../components/SelectorImagenes'
import { ErrorConReintento } from '../components/EstadoCarga'
import { Aviso, Button, CargandoPagina } from '../components/ui'
import {
  editarPropiedad,
  eliminarImagenPropiedad,
  listarMisPropiedades,
  subirImagenPropiedad,
} from '../services/propiedades'
import { liberarPreview } from '../utils/imagenes'
import {
  armarPropiedadRequest,
  enfocarPrimerError,
  datosDesdePropiedad,
  erroresDelBackend,
  validarPropiedad,
} from '../utils/formularioPropiedad'

function NoEsTuya() {
  return (
    <div className="py-16 text-center">
      <h1 className="text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em]">No encontramos esa propiedad</h1>
      <p className="mt-2 text-gray-500">No existe o no la publicaste vos, así que no la podés editar.</p>
      <Button to="/mis-propiedades" className="mt-6">
        Ir a mis propiedades
      </Button>
    </div>
  )
}

/** Fotos que la propiedad ya tiene: se pueden marcar para quitar (se quitan al guardar). */
function FotosActuales({ fotos, aQuitar, onAlternar, bloqueado }) {
  if (fotos.length === 0) return <p className="text-sm text-gray-500">La propiedad todavía no tiene fotos.</p>

  return (
    <ol className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {fotos.map((foto, indice) => {
        const quitar = aQuitar.has(foto.id)
        return (
          <li
            key={foto.id}
            className={`relative aspect-[4/3] overflow-hidden rounded-xl border bg-gray-100 ${
              indice === 0 && !quitar ? 'border-rio-500 ring-2 ring-rio-200' : 'border-gray-200'
            }`}
          >
            <img
              src={foto.url}
              alt={`Foto ${indice + 1} de la propiedad`}
              className={`h-full w-full object-cover ${quitar ? 'opacity-30 grayscale' : ''}`}
            />
            <span className="absolute left-2 top-2 rounded bg-black/60 px-1.5 py-0.5 text-xs font-bold text-white">
              {quitar ? 'Se va a quitar' : indice === 0 ? 'Portada' : indice + 1}
            </span>
            <div className="absolute inset-x-0 bottom-0 flex justify-end bg-gradient-to-t from-black/50 to-transparent p-2">
              <button
                type="button"
                onClick={() => onAlternar(foto.id)}
                disabled={bloqueado}
                className="rounded-full bg-white/90 px-3 py-1 text-xs font-bold text-gray-800 shadow hover:bg-white disabled:opacity-40 focus:outline-none focus-visible:ring-2 focus-visible:ring-rio-500"
              >
                {quitar ? 'Deshacer' : 'Quitar'}
              </button>
            </div>
          </li>
        )
      })}
    </ol>
  )
}

/**
 * Editar una propiedad propia. Usa los mismos campos que publicar.
 *
 * Al guardar: 1) PUT con los datos, 2) se quitan las fotos marcadas, 3) se suben las nuevas en orden.
 * Si falla algo de las fotos, los datos ya quedaron guardados: se puede reintentar solo lo que faltó.
 */
export default function EditarPropiedad() {
  const { id } = useParams()
  const navigate = useNavigate()

  // { estado: 'ok' | 'no-encontrada' | 'error', error? }
  const [carga, setCarga] = useState(null)
  const [intento, setIntento] = useState(0)

  const [datos, setDatos] = useState(null)
  const [ubicacion, setUbicacion] = useState(null)
  const [fotos, setFotos] = useState([]) // las que ya tiene: [{ id, url, orden }]
  const [aQuitar, setAQuitar] = useState(() => new Set())
  const [nuevas, setNuevas] = useState([])
  const [estadosNuevas, setEstadosNuevas] = useState({}) // { [id]: 'subiendo' | 'subida' | 'error' }

  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [errorFotos, setErrorFotos] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [datosGuardados, setDatosGuardados] = useState(false)

  // Al salir se liberan las vistas previas de las fotos nuevas
  const nuevasRef = useRef(nuevas)
  useEffect(() => {
    nuevasRef.current = nuevas
  }, [nuevas])
  useEffect(() => () => nuevasRef.current.forEach(liberarPreview), [])

  // Se busca entre "mis propiedades": así viene la ubicación exacta y las fotos con su id,
  // y si no es del usuario directamente no aparece.
  useEffect(() => {
    let cancelado = false
    listarMisPropiedades()
      .then((propias) => {
        if (cancelado) return
        const propiedad = (propias ?? []).find((p) => String(p.id) === String(id))
        if (!propiedad) {
          setCarga({ estado: 'no-encontrada' })
          return
        }
        setDatos(datosDesdePropiedad(propiedad))
        setUbicacion(
          propiedad.latitud != null && propiedad.longitud != null
            ? { latitud: propiedad.latitud, longitud: propiedad.longitud }
            : null,
        )
        setFotos(propiedad.fotos ?? [])
        setCarga({ estado: 'ok' })
      })
      .catch((e) => {
        if (!cancelado) setCarga({ estado: 'error', error: e.mensaje || 'No se pudo cargar la propiedad' })
      })
    return () => {
      cancelado = true
    }
  }, [id, intento])

  if (!carga) return <CargandoPagina />
  if (carga.estado === 'no-encontrada') return <NoEsTuya />
  if (carga.estado === 'error') {
    return (
      <ErrorConReintento
        mensaje={carga.error}
        onReintentar={() => {
          setCarga(null)
          setIntento((n) => n + 1)
        }}
      />
    )
  }

  const cambiar = (campo, valor) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }))
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined }))
  }

  const alternarQuitar = (fotoId) =>
    setAQuitar((anteriores) => {
      const siguiente = new Set(anteriores)
      if (siguiente.has(fotoId)) siguiente.delete(fotoId)
      else siguiente.add(fotoId)
      return siguiente
    })

  const irAlDetalle = () => navigate(`/propiedades/${id}`, { replace: true, state: { editada: true } })

  /** Quita las fotos marcadas y sube las nuevas en orden. Devuelve true si salió todo bien. */
  const aplicarFotos = async () => {
    for (const fotoId of aQuitar) {
      try {
        await eliminarImagenPropiedad(id, fotoId)
        setFotos((anteriores) => anteriores.filter((f) => f.id !== fotoId))
        setAQuitar((anteriores) => {
          const siguiente = new Set(anteriores)
          siguiente.delete(fotoId)
          return siguiente
        })
      } catch (e) {
        setErrorFotos(`No se pudo quitar una foto: ${e.mensaje || 'error desconocido'}`)
        return false
      }
    }

    const estados = { ...estadosNuevas }
    for (const imagen of nuevas) {
      if (estados[imagen.id] === 'subida') continue
      estados[imagen.id] = 'subiendo'
      setEstadosNuevas({ ...estados })
      try {
        await subirImagenPropiedad(id, imagen.archivo)
        estados[imagen.id] = 'subida'
        setEstadosNuevas({ ...estados })
      } catch (e) {
        estados[imagen.id] = 'error'
        setEstadosNuevas({ ...estados })
        setErrorFotos(`No se pudo subir ${imagen.archivo.name}: ${e.mensaje || 'error desconocido'}`)
        return false
      }
    }
    return true
  }

  const guardar = async (event) => {
    event.preventDefault()
    setErrorGeneral(null)
    setErrorFotos(null)

    // Los datos ya se guardaron: solo se reintenta lo que faltó de las fotos
    if (datosGuardados) {
      setEnviando(true)
      if (await aplicarFotos()) irAlDetalle()
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
    try {
      await editarPropiedad(id, armarPropiedadRequest(datos, ubicacion))
    } catch (e) {
      const { porCampo, otros } = erroresDelBackend(e.detalles)
      setErrores(porCampo)
      enfocarPrimerError(porCampo)
      if (Object.keys(porCampo).length === 0 || otros.length > 0) {
        setErrorGeneral(otros.length > 0 ? otros.join('. ') : e.mensaje || 'No se pudieron guardar los cambios')
      }
      setEnviando(false)
      return
    }

    setDatosGuardados(true)
    if (await aplicarFotos()) irAlDetalle()
    else setEnviando(false)
  }

  const fotosQueQuedan = fotos.length - aQuitar.size
  const nuevasSinSubir = nuevas.filter((imagen) => estadosNuevas[imagen.id] !== 'subida').length

  let textoBoton = 'Guardar cambios'
  if (datosGuardados && !enviando) textoBoton = 'Reintentar fotos'
  if (enviando) textoBoton = datosGuardados ? 'Actualizando fotos...' : 'Guardando...'

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-[2.25rem] leading-tight font-extrabold tracking-[-0.03em]">Editar propiedad</h1>
      <p className="mt-1 text-sm text-gray-600">
        Los campos marcados como opcionales se pueden dejar vacíos.
      </p>

      <form onSubmit={guardar} noValidate className="mt-6 space-y-6">
        <fieldset disabled={enviando || datosGuardados} className="space-y-6">
          <CamposPropiedad
            datos={datos}
            errores={errores}
            onCambiar={cambiar}
            ubicacion={ubicacion}
            onCambiarUbicacion={setUbicacion}
            ubicacionGuardada
          />
        </fieldset>

        <Seccion titulo="Fotos">
          <FotosActuales fotos={fotos} aQuitar={aQuitar} onAlternar={alternarQuitar} bloqueado={enviando} />
          <div className="pt-2">
            <p className="mb-2 text-sm font-bold text-gray-800">Agregar fotos</p>
            <SelectorImagenes
              imagenes={nuevas}
              onChange={setNuevas}
              estados={estadosNuevas}
              bloqueado={enviando || datosGuardados}
              yaSubidas={fotosQueQuedan}
            />
          </div>
        </Seccion>

        {errorFotos && (
          <Aviso tipo="advertencia" titulo="Los datos se guardaron, pero no se pudieron actualizar todas las fotos.">
            <p>{errorFotos}</p>
            <p className="mt-1">
              Podés reintentar o ir a la propiedad
              {nuevasSinSubir > 0 && ` sin las ${nuevasSinSubir} fotos nuevas que faltan`}.
            </p>
          </Aviso>
        )}

        {errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}

        <div className="flex justify-end gap-2">
          {datosGuardados ? (
            <Button variante="fantasma" onClick={irAlDetalle} disabled={enviando}>
              Ir a la propiedad
            </Button>
          ) : (
            <Button variante="fantasma" to="/mis-propiedades">
              Cancelar
            </Button>
          )}
          <Button type="submit" cargando={enviando}>
            {textoBoton}
          </Button>
        </div>
      </form>
    </div>
  )
}
