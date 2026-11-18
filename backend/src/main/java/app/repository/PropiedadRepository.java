package app.repository;

import app.model.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {
    boolean existsByUrlOriginal(String urlOriginal);
}
