package ma.location.karflow.repository;

import ma.location.karflow.model.Fiche;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FicheRepository extends JpaRepository<Fiche, Long> {
}

