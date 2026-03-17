package ma.karflow.feature.rental.repository;

import ma.karflow.feature.rental.entity.Insurance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InsuranceRepository extends JpaRepository<Insurance, UUID> {
    Page<Insurance> findByTenantId(UUID tenantId, Pageable pageable);
    Optional<Insurance> findByIdAndTenantId(UUID id, UUID tenantId);
}
