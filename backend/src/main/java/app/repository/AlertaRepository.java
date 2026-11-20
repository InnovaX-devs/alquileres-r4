package app.repository;

import app.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByActivaTrue();

    List<Alerta> findByUsuarioIdOrderByCreadaEnDesc(Long usuarioId);
}
