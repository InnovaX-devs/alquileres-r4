package app.repository;

import app.model.ImagenPropiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImagenPropiedadRepository extends JpaRepository<ImagenPropiedad, Long> {

    List<ImagenPropiedad> findByPropiedadIdOrderByOrdenAscIdAsc(Long propiedadId);

    long countByPropiedadId(Long propiedadId);

    /** Mayor orden usado en la propiedad, o 0 si todavía no tiene imágenes. */
    @Query("select coalesce(max(i.orden), 0) from ImagenPropiedad i where i.propiedad.id = :propiedadId")
    int findMaxOrdenByPropiedadId(@Param("propiedadId") Long propiedadId);
}
