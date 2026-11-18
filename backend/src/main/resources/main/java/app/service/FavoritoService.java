package app.service;

import app.model.FavoritoPropiedad;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.FavoritoPropiedadRepository;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoritoService {

    private final FavoritoPropiedadRepository favoritoPropiedadRepository;
    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;

    public FavoritoService(FavoritoPropiedadRepository favoritoPropiedadRepository,
                           PropiedadRepository propiedadRepository,
                           UsuarioRepository usuarioRepository) {
        this.favoritoPropiedadRepository = favoritoPropiedadRepository;
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public FavoritoPropiedad guardarPropiedadFavorita(Long propiedadId, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"));

        if (favoritoPropiedadRepository.existsByUsuarioAndPropiedad(usuario, propiedad)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La propiedad ya está en tus favoritos");
        }

        FavoritoPropiedad favorito = new FavoritoPropiedad();
        favorito.setUsuario(usuario);
        favorito.setPropiedad(propiedad);

        try {
            return favoritoPropiedadRepository.save(favorito);
        } catch (DataIntegrityViolationException e) {
            // Dos requests simultáneos: la restricción única de la tabla evita el duplicado
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La propiedad ya está en tus favoritos");
        }
    }
}
