package ma.karflow.feature.vehicle.repository;

import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Page<Vehicle> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByLicensePlateAndTenantId(String licensePlate, UUID tenantId);

    Page<Vehicle> findByTenantIdAndStatus(UUID tenantId, VehicleStatus status, Pageable pageable);

    Page<Vehicle> findByTenantIdAndCategoryId(UUID tenantId, UUID categoryId, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.tenantId = :tenantId " +
            "AND (:status IS NULL OR v.status = :status) " +
            "AND (:categoryId IS NULL OR v.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR v.vehicleModel.brand.id = :brandId)")
    Page<Vehicle> findWithFilters(@Param("tenantId") UUID tenantId,
                                  @Param("status") VehicleStatus status,
                                  @Param("categoryId") UUID categoryId,
                                  @Param("brandId") UUID brandId,
                                  Pageable pageable);

    long countByTenantIdAndStatus(UUID tenantId, VehicleStatus status);
}
