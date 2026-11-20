package app.repository;

import app.model.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long>, JpaSpecificationExecutor<Propiedad> {
    boolean existsByUrlOriginal(String urlOriginal);

    List<Propiedad> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    /** Propiedades creadas por el usuario o publicadas por su inmobiliaria. */
    @Query("""
            select p from Propiedad p
            left join p.usuario u
            left join p.inmobiliaria i
            where u.id = :usuarioId or i.id = :inmobiliariaId
            order by p.id desc""")
    List<Propiedad> findDelUsuarioOSuInmobiliaria(@Param("usuarioId") Long usuarioId,
                                                  @Param("inmobiliariaId") Long inmobiliariaId);
}
