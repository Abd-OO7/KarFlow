package ma.karflow.feature.vehicle.repository;

import ma.karflow.feature.vehicle.entity.VehicleStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleStatusHistoryRepository extends JpaRepository<VehicleStatusHistory, UUID> {

    Page<VehicleStatusHistory> findByVehicleIdAndTenantIdOrderByChangeDateDesc(
            UUID vehicleId, UUID tenantId, Pageable pageable);
}
