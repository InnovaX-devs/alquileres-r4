package app.service;

import app.controller.dto.ActualizarInmobiliariaRequest;
import app.controller.dto.InmobiliariaRequest;
import app.model.Inmobiliaria;
import app.model.RolInmobiliaria;
import app.model.Usuario;
import app.repository.InmobiliariaRepository;
import app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Inmobiliarias y sus miembros (issue #61).
 *
 * Roles dentro de la inmobiliaria ({@link RolInmobiliaria}):
 * - ADMINISTRADOR: quien la crea. Edita sus datos y agrega o quita miembros.
 * - AGENTE: usuario vinculado por un administrador. Publica a nombre de la inmobiliaria y gestiona
 *   sus solicitudes de visita (ver Propiedad#esPublicadaPor), pero no puede tocar los datos ni los miembros.
 *
 * La verificación de la inmobiliaria la decide solo un ADMIN de la plataforma.
 */
@Service
public class InmobiliariaService {

    private static final Logger log = LoggerFactory.getLogger(InmobiliariaService.class);

    private final InmobiliariaRepository inmobiliariaRepository;
    private final UsuarioRepository usuarioRepository;

    public InmobiliariaService(InmobiliariaRepository inmobiliariaRepository,
                               UsuarioRepository usuarioRepository) {
        this.inmobiliariaRepository = inmobiliariaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ── Alta ─────────────────────────────────────────────────────────────────

    /**
     * Crea la inmobiliaria y deja al usuario autenticado como su ADMINISTRADOR, en una misma transacción:
     * si falla la vinculación, no queda una inmobiliaria sin dueño.
     */
    @Transactional
    public Inmobiliaria crearYVincular(InmobiliariaRequest request, String emailUsuario) {
        Usuario usuario = usuarioAutenticado(emailUsuario);

        if (usuario.getInmobiliaria() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya estás asociado a una inmobiliaria");
        }

        String email = normalizarEmail(request.getEmail());
        if (inmobiliariaRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una inmobiliaria con ese email");
        }

        Inmobiliaria inmobiliaria = new Inmobiliaria();
        inmobiliaria.setNombre(request.getNombre().trim());
        inmobiliaria.setEmail(email);
        inmobiliaria.setTelefono(textoOpcional(request.getTelefono()));
        inmobiliaria.setLogoUrl(textoOpcional(request.getLogoUrl()));
        // Queda pendiente de verificación (false por defecto) hasta que la apruebe un ADMIN.

        Inmobiliaria guardada = inmobiliariaRepository.save(inmobiliaria);

        usuario.setInmobiliaria(guardada);
        usuario.setRolInmobiliaria(RolInmobiliaria.ADMINISTRADOR);
        usuarioRepository.save(usuario);

        log.info("Inmobiliaria creada: inmobiliariaId={}, administradorId={}", guardada.getId(), usuario.getId());
        return guardada;
    }

    // ── Datos de la inmobiliaria (solo ADMINISTRADOR) ────────────────────────

    /**
     * Actualiza los campos informados. Si cambia el nombre o el email, la inmobiliaria vuelve a quedar
     * pendiente de verificación: lo que verificó el ADMIN ya no es lo mismo que se muestra.
     */
    @Transactional
    public Inmobiliaria actualizar(Long inmobiliariaId, ActualizarInmobiliariaRequest request, String emailUsuario) {
        Inmobiliaria inmobiliaria = obtener(inmobiliariaId);
        exigirAdministrador(usuarioAutenticado(emailUsuario), inmobiliaria);

        boolean cambioIdentidad = false;

        if (request.getNombre() != null) {
            String nombre = request.getNombre().trim();
            cambioIdentidad |= !nombre.equals(inmobiliaria.getNombre());
            inmobiliaria.setNombre(nombre);
        }
        if (request.getEmail() != null) {
            String email = normalizarEmail(request.getEmail());
            if (!email.equals(inmobiliaria.getEmail())) {
                if (inmobiliariaRepository.existsByEmail(email)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una inmobiliaria con ese email");
                }
                inmobiliaria.setEmail(email);
                cambioIdentidad = true;
            }
        }
        if (request.getTelefono() != null) inmobiliaria.setTelefono(textoOpcional(request.getTelefono()));
        if (request.getLogoUrl() != null) inmobiliaria.setLogoUrl(textoOpcional(request.getLogoUrl()));

        if (cambioIdentidad && inmobiliaria.isVerificada()) {
            inmobiliaria.setVerificada(false);
            log.info("Inmobiliaria vuelve a pendiente de verificación por cambio de datos: id={}", inmobiliariaId);
        }

        return inmobiliariaRepository.save(inmobiliaria);
    }

    // ── Miembros ─────────────────────────────────────────────────────────────

    /** Lista de miembros. La puede ver cualquier miembro de la inmobiliaria. */
    @Transactional(readOnly = true)
    public List<Usuario> listarMiembros(Long inmobiliariaId, String emailUsuario) {
        Inmobiliaria inmobiliaria = obtener(inmobiliariaId);
        if (!usuarioAutenticado(emailUsuario).esMiembroDe(inmobiliaria.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenecés a esta inmobiliaria");
        }
        return usuarioRepository.findByInmobiliariaIdOrderByNombreAsc(inmobiliariaId);
    }

    /** Vincula como AGENTE a un usuario ya registrado que no pertenezca a otra inmobiliaria. */
    @Transactional
    public Usuario agregarMiembro(Long inmobiliariaId, String emailNuevoMiembro, String emailUsuario) {
        Inmobiliaria inmobiliaria = obtener(inmobiliariaId);
        exigirAdministrador(usuarioAutenticado(emailUsuario), inmobiliaria);

        Usuario nuevo = usuarioRepository.findByEmail(normalizarEmail(emailNuevoMiembro))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un usuario registrado con ese email"));

        if (nuevo.esMiembroDe(inmobiliariaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es miembro de esta inmobiliaria");
        }
        if (nuevo.getInmobiliaria() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya pertenece a otra inmobiliaria");
        }

        nuevo.setInmobiliaria(inmobiliaria);
        nuevo.setRolInmobiliaria(RolInmobiliaria.AGENTE);
        Usuario guardado = usuarioRepository.save(nuevo);

        log.info("Miembro agregado: inmobiliariaId={}, usuarioId={}", inmobiliariaId, guardado.getId());
        return guardado;
    }

    /**
     * Desvincula a un miembro. Las propiedades que publicó a nombre de la inmobiliaria siguen siendo
     * de la inmobiliaria; el usuario pierde acceso a ellas.
     * El administrador no puede quitarse a sí mismo, para que la inmobiliaria no quede sin quien la gestione.
     */
    @Transactional
    public void quitarMiembro(Long inmobiliariaId, Long usuarioId, String emailUsuario) {
        Inmobiliaria inmobiliaria = obtener(inmobiliariaId);
        Usuario administrador = usuarioAutenticado(emailUsuario);
        exigirAdministrador(administrador, inmobiliaria);

        if (administrador.getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No podés quitarte a vos mismo de la inmobiliaria");
        }

        Usuario miembro = usuarioRepository.findById(usuarioId)
                .filter(u -> u.esMiembroDe(inmobiliariaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El usuario no es miembro de esta inmobiliaria"));

        miembro.setInmobiliaria(null);
        miembro.setRolInmobiliaria(null);
        usuarioRepository.save(miembro);

        log.info("Miembro quitado: inmobiliariaId={}, usuarioId={}", inmobiliariaId, usuarioId);
    }

    // ── Verificación (solo ADMIN de la plataforma, ver SecurityConfig) ──────

    @Transactional
    public Inmobiliaria cambiarVerificacion(Long inmobiliariaId, boolean verificada) {
        Inmobiliaria inmobiliaria = obtener(inmobiliariaId);
        inmobiliaria.setVerificada(verificada);
        Inmobiliaria guardada = inmobiliariaRepository.save(inmobiliaria);
        log.info("Verificación de inmobiliaria actualizada: id={}, verificada={}", inmobiliariaId, verificada);
        return guardada;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Inmobiliaria obtener(Long id) {
        return inmobiliariaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inmobiliaria no encontrada"));
    }

    private Usuario usuarioAutenticado(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private static void exigirAdministrador(Usuario usuario, Inmobiliaria inmobiliaria) {
        if (!usuario.esAdministradorDe(inmobiliaria.getId())) {
            log.warn("Acción de administrador denegada: inmobiliariaId={}, usuarioId={}",
                    inmobiliaria.getId(), usuario.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el administrador de la inmobiliaria puede realizar esta acción");
        }
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
