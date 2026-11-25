const TIPOS = {
  exito: { clases: 'border-rio-200 bg-rio-50 text-rio-900', rol: 'status' },
  info: { clases: 'border-gray-200 bg-white text-gray-800', rol: 'status' },
  advertencia: { clases: 'border-sol-300 bg-sol-100 text-pino-800', rol: 'status' },
  error: { clases: 'border-red-200 bg-red-50 text-red-800', rol: 'alert' },
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
    <div role={rol} className={`animate-aparecer rounded-xl border px-4 py-3 text-[15px] ${clases} ${className}`.trim()}>
      {titulo && <p className="font-bold">{titulo}</p>}
      {children && <div className={titulo ? 'mt-1' : ''}>{children}</div>}
    </div>
  )
}
