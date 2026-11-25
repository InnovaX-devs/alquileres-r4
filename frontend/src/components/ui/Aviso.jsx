const TIPOS = {
  exito: { clases: 'border-green-200 bg-green-50 text-green-800', rol: 'status' },
  info: { clases: 'border-blue-200 bg-blue-50 text-blue-900', rol: 'status' },
  advertencia: { clases: 'border-yellow-200 bg-yellow-50 text-yellow-800', rol: 'status' },
  error: { clases: 'border-red-200 bg-red-50 text-red-700', rol: 'alert' },
}

/**
 * Mensaje destacado (#87): "Cuenta creada", "Tu sesión expiró", errores generales de un formulario.
 * Los de error se anuncian de inmediato a los lectores de pantalla (role="alert").
 *
 * @param {'exito'|'info'|'advertencia'|'error'} [props.tipo='info']
 * @param {node} [props.titulo]  primera línea en negrita
 */
export default function Aviso({ tipo = 'info', titulo, className = '', children }) {
  const { clases, rol } = TIPOS[tipo]
  return (
    <div role={rol} className={`rounded-md border px-4 py-3 text-sm ${clases} ${className}`.trim()}>
      {titulo && <p className="font-medium">{titulo}</p>}
      {children && <div className={titulo ? 'mt-1' : ''}>{children}</div>}
    </div>
  )
}
