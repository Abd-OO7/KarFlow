package ma.karflow.feature.vehicle.repository;

import ma.karflow.feature.vehicle.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Page<Category> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Category> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);

    @Query("SELECT c FROM Category c WHERE c.deleted = false")
    List<Category> findAllActive();
}
