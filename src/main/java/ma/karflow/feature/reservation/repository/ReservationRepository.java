package ma.karflow.feature.reservation.repository;

import ma.karflow.feature.reservation.entity.Reservation;
import ma.karflow.feature.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Page<Reservation> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Reservation> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Reservation> findByClientIdAndTenantId(UUID clientId, UUID tenantId, Pageable pageable);

    Page<Reservation> findByTenantIdAndStatus(UUID tenantId, ReservationStatus status, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.client.email = :email AND r.deleted = false")
    Page<Reservation> findByClientEmail(@Param("email") String email, Pageable pageable);
}
