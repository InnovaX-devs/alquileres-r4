package app.service;

import app.controller.dto.InmobiliariaRequest;
import app.model.Inmobiliaria;
import app.model.Usuario;
import app.repository.InmobiliariaRepository;
import app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Inmobiliarias (issue #61): un usuario autenticado crea su inmobiliaria y queda vinculado a ella.
 * A partir de ahí, las propiedades que publique quedan a nombre de la inmobiliaria
 * (ver PropiedadService#publicar).
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

    /**
     * Crea la inmobiliaria y la asocia al usuario autenticado, todo en una misma transacción:
     * si falla la vinculación, no queda una inmobiliaria sin dueño.
     */
    @Transactional
    public Inmobiliaria crearYVincular(InmobiliariaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (usuario.getInmobiliaria() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya estás asociado a una inmobiliaria");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (inmobiliariaRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una inmobiliaria con ese email");
        }

        Inmobiliaria inmobiliaria = new Inmobiliaria();
        inmobiliaria.setNombre(request.getNombre().trim());
        inmobiliaria.setEmail(email);
        inmobiliaria.setTelefono(textoOpcional(request.getTelefono()));
        inmobiliaria.setLogoUrl(textoOpcional(request.getLogoUrl()));
        // La verificación no la decide quien crea la inmobiliaria: queda en false (valor por defecto).

        Inmobiliaria guardada = inmobiliariaRepository.save(inmobiliaria);

        usuario.setInmobiliaria(guardada);
        usuarioRepository.save(usuario);

        log.info("Inmobiliaria creada y vinculada: inmobiliariaId={}, usuarioId={}", guardada.getId(), usuario.getId());
        return guardada;
    }

    private static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
