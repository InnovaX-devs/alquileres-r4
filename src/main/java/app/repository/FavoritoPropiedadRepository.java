package app.repository;

import app.model.FavoritoPropiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoritoPropiedadRepository extends JpaRepository<FavoritoPropiedad, Long> {

    List<FavoritoPropiedad> findByUsuarioIdOrderByFechaGuardadoDesc(Long usuarioId);

    Optional<FavoritoPropiedad> findByUsuarioIdAndPropiedadId(Long usuarioId, Long propiedadId);

    boolean existsByUsuarioIdAndPropiedadId(Long usuarioId, Long propiedadId);
}
