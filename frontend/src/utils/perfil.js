// Reglas del perfil de usuario compartidas por la pantalla de perfil y su formulario (#71).

/**
 * Datos que tienen que estar completos para que el perfil quede verificado.
 * Copia la regla del backend: UsuarioService.perfilEstaCompleto() (nombre, teléfono y documento).
 * Si cambia allá, hay que cambiarla acá.
 */
export const CAMPOS_PARA_VERIFICAR = [
  { clave: 'nombre', label: 'Nombre' },
  { clave: 'telefono', label: 'Teléfono' },
  { clave: 'documento', label: 'Documento' },
]

export function estaCompleto(valor) {
  return typeof valor === 'string' && valor.trim() !== ''
}

/** Labels de los datos que le faltan al usuario para quedar verificado (vacío si no falta nada). */
export function datosFaltantesParaVerificar(usuario) {
  return CAMPOS_PARA_VERIFICAR.filter(({ clave }) => !estaCompleto(usuario?.[clave])).map(
    ({ label }) => label,
  )
}

/** "Teléfono", "Teléfono y Documento", "Nombre, Teléfono y Documento" */
export function unirConY(items) {
  if (items.length <= 1) return items.join('')
  return `${items.slice(0, -1).join(', ')} y ${items.at(-1)}`
}
