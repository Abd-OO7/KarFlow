package ma.karflow.feature.rental.repository;

import ma.karflow.feature.rental.entity.Insurance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsuranceRepository extends JpaRepository<Insurance, UUID> {
    Page<Insurance> findByTenantId(UUID tenantId, Pageable pageable);
    Optional<Insurance> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByNameAndTenantId(String name, UUID tenantId);

    @Query("SELECT i FROM Insurance i WHERE i.deleted = false")
    List<Insurance> findAllActive();
}
