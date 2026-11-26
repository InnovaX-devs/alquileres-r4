package app.repository;

import app.model.FavoritoPropiedad;
import app.model.Propiedad;
import app.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FavoritoPropiedadRepository extends JpaRepository<FavoritoPropiedad, Long> {
    boolean existsByUsuarioAndPropiedad(Usuario usuario, Propiedad propiedad);
    Optional<FavoritoPropiedad> findByUsuarioAndPropiedad(Usuario usuario, Propiedad propiedad);
    List<FavoritoPropiedad> findByUsuario(Usuario usuario);

    @EntityGraph(attributePaths = "propiedad")
    List<FavoritoPropiedad> findByUsuarioOrderByFechaGuardadoDesc(Usuario usuario);

    /** Al eliminar una propiedad se borra de los favoritos de todos. */
    @Transactional
    @Modifying
    @Query("delete from FavoritoPropiedad f where f.propiedad.id = :propiedadId")
    int deleteByPropiedadId(@Param("propiedadId") Long propiedadId);
}
