package app.repository;

import app.model.Inmobiliaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InmobiliariaRepository extends JpaRepository<Inmobiliaria, Long> {
    boolean existsByEmail(String email);
}