package ma.karflow.feature.vehicle.repository;

import ma.karflow.feature.vehicle.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Page<Brand> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Brand> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);
}
