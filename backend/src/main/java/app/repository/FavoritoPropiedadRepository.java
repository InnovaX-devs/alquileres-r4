package app.repository;

import app.model.FavoritoPropiedad;
import app.model.Propiedad;
import app.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoPropiedadRepository extends JpaRepository<FavoritoPropiedad, Long> {
    boolean existsByUsuarioAndPropiedad(Usuario usuario, Propiedad propiedad);
    Optional<FavoritoPropiedad> findByUsuarioAndPropiedad(Usuario usuario, Propiedad propiedad);
    List<FavoritoPropiedad> findByUsuario(Usuario usuario);

    @EntityGraph(attributePaths = "propiedad")
    List<FavoritoPropiedad> findByUsuarioOrderByFechaGuardadoDesc(Usuario usuario);
}
