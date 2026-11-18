package app.repository;

import app.model.FavoritoInmobiliaria;
import app.model.Inmobiliaria;
import app.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoInmobiliariaRepository extends JpaRepository<FavoritoInmobiliaria, Long> {
    boolean existsByUsuarioAndInmobiliaria(Usuario usuario, Inmobiliaria inmobiliaria);
    Optional<FavoritoInmobiliaria> findByUsuarioAndInmobiliaria(Usuario usuario, Inmobiliaria inmobiliaria);
    List<FavoritoInmobiliaria> findByUsuario(Usuario usuario);

    @EntityGraph(attributePaths = "inmobiliaria")
    List<FavoritoInmobiliaria> findByUsuarioOrderByFechaGuardadoDesc(Usuario usuario);
}