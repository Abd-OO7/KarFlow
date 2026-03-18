package ma.karflow.feature.vehicle.repository;

import ma.karflow.feature.vehicle.entity.VehicleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, UUID> {

    Page<VehicleModel> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<VehicleModel> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<VehicleModel> findByBrandIdAndTenantId(UUID brandId, UUID tenantId, Pageable pageable);
}
