package app.repository;

import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudVisitaRepository extends JpaRepository<SolicitudVisita, Long> {
    boolean existsByUsuarioAndPropiedad(Usuario usuario, Propiedad propiedad);

    /** Solicitudes sobre propiedades creadas por el usuario (issue #57). */
    @Query("""
            select s from SolicitudVisita s
            join fetch s.propiedad p
            join fetch s.usuario
            left join p.usuario pu
            where pu.id = :usuarioId
            order by s.fechaSolicitud desc""")
    List<SolicitudVisita> findRecibidasPorUsuario(@Param("usuarioId") Long usuarioId);

    /** Solicitudes sobre propiedades creadas por el usuario o publicadas por su inmobiliaria (issue #57). */
    @Query("""
            select s from SolicitudVisita s
            join fetch s.propiedad p
            join fetch s.usuario
            left join p.usuario pu
            left join p.inmobiliaria pi
            where pu.id = :usuarioId or pi.id = :inmobiliariaId
            order by s.fechaSolicitud desc""")
    List<SolicitudVisita> findRecibidasPorUsuarioOInmobiliaria(@Param("usuarioId") Long usuarioId,
                                                               @Param("inmobiliariaId") Long inmobiliariaId);
}
