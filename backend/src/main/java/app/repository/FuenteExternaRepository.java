package app.repository;

import app.model.FuenteExterna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuenteExternaRepository extends JpaRepository<FuenteExterna, Long> {
    boolean existsByNombre(String nombre);
    List<FuenteExterna> findByActivaTrue();
}
