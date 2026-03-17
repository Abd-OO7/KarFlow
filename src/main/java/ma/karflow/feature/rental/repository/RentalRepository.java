package ma.karflow.feature.rental.repository;

import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.enums.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalRepository extends JpaRepository<Rental, UUID> {

    Page<Rental> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Rental> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Rental> findByTenantIdAndStatus(UUID tenantId, RentalStatus status, Pageable pageable);

    Page<Rental> findByClientIdAndTenantId(UUID clientId, UUID tenantId, Pageable pageable);

    @Query("SELECT r FROM Rental r WHERE r.tenantId = :tenantId " +
            "AND r.vehicle.id = :vehicleId " +
            "AND r.status IN ('RESERVED', 'ACTIVE') " +
            "AND r.startDate <= :endDate AND r.endDate >= :startDate")
    List<Rental> findOverlappingRentals(@Param("tenantId") UUID tenantId,
                                        @Param("vehicleId") UUID vehicleId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    long countByTenantIdAndStatus(UUID tenantId, RentalStatus status);

    @Query("SELECT r FROM Rental r WHERE r.tenantId = :tenantId AND r.status = 'ACTIVE' AND r.endDate < :today")
    List<Rental> findOverdueRentals(@Param("tenantId") UUID tenantId, @Param("today") LocalDate today);
}
