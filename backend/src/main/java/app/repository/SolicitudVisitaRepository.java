package app.repository;

import app.model.Propiedad;
import app.model.SolicitudVisita;
import app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudVisitaRepository extends JpaRepository<SolicitudVisita, Long> {
    boolean existsByUsuarioAndPropiedad(Usuario usuario, Propiedad propiedad);
}