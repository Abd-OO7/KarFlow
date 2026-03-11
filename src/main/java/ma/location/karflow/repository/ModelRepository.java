package ma.location.karflow.repository;

import ma.location.karflow.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {
}

