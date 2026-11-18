package app.service;

import app.controller.dto.FavoritosResponse;
import app.controller.dto.InmobiliariaFavoritaResponse;
import app.controller.dto.PropiedadFavoritaResponse;
import app.model.FavoritoInmobiliaria;
import app.model.FavoritoPropiedad;
import app.model.Inmobiliaria;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.FavoritoInmobiliariaRepository;
import app.repository.FavoritoPropiedadRepository;
import app.repository.InmobiliariaRepository;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoPropiedadRepository favoritoPropiedadRepository;
    private final FavoritoInmobiliariaRepository favoritoInmobiliariaRepository;
    private final PropiedadRepository propiedadRepository;
    private final InmobiliariaRepository inmobiliariaRepository;
    private final UsuarioRepository usuarioRepository;

    public FavoritoService(FavoritoPropiedadRepository favoritoPropiedadRepository,
                           FavoritoInmobiliariaRepository favoritoInmobiliariaRepository,
                           PropiedadRepository propiedadRepository,
                           InmobiliariaRepository inmobiliariaRepository,
                           UsuarioRepository usuarioRepository) {
        this.favoritoPropiedadRepository = favoritoPropiedadRepository;
        this.favoritoInmobiliariaRepository = favoritoInmobiliariaRepository;
        this.propiedadRepository = propiedadRepository;
        this.inmobiliariaRepository = inmobiliariaRepository;
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

    public FavoritoInmobiliaria guardarInmobiliariaFavorita(Long inmobiliariaId, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Inmobiliaria inmobiliaria = inmobiliariaRepository.findById(inmobiliariaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inmobiliaria no encontrada"));

        if (favoritoInmobiliariaRepository.existsByUsuarioAndInmobiliaria(usuario, inmobiliaria)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La inmobiliaria ya está en tus favoritos");
        }

        FavoritoInmobiliaria favorito = new FavoritoInmobiliaria();
        favorito.setUsuario(usuario);
        favorito.setInmobiliaria(inmobiliaria);

        try {
            return favoritoInmobiliariaRepository.save(favorito);
        } catch (DataIntegrityViolationException e) {
            // Dos requests simultáneos: la restricción única de la tabla evita el duplicado
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La inmobiliaria ya está en tus favoritos");
        }
    }

    @Transactional(readOnly = true)
    public FavoritosResponse listarFavoritos(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        List<PropiedadFavoritaResponse> propiedades = favoritoPropiedadRepository
                .findByUsuarioOrderByFechaGuardadoDesc(usuario).stream()
                .map(PropiedadFavoritaResponse::from)
                .toList();

        List<InmobiliariaFavoritaResponse> inmobiliarias = favoritoInmobiliariaRepository
                .findByUsuarioOrderByFechaGuardadoDesc(usuario).stream()
                .map(InmobiliariaFavoritaResponse::from)
                .toList();

        return new FavoritosResponse(propiedades, inmobiliarias);
    }
}
