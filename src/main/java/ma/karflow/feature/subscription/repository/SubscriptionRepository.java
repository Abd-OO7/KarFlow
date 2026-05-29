package ma.karflow.feature.subscription.repository;

import ma.karflow.feature.subscription.entity.Subscription;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByOrganisationIdAndTenantId(UUID orgId, UUID tenantId);

    Optional<Subscription> findByTenantId(UUID tenantId);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.deleted = false")
    List<Subscription> findAllByStatus(@Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = ma.karflow.feature.subscription.enums.SubscriptionStatus.TRIAL AND s.trialEndDate BETWEEN :from AND :to AND s.deleted = false")
    List<Subscription> findTrialsExpiringBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT tenant_id FROM subscription WHERE status NOT IN ('SUSPENDED', 'CANCELLED', 'EXPIRED') AND deleted = FALSE", nativeQuery = true)
    List<byte[]> findVisibleTenantIdsRaw();

    default List<UUID> findVisibleTenantIds() {
        return findVisibleTenantIdsRaw().stream()
                .map(bytes -> {
                    java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
                    return new UUID(bb.getLong(), bb.getLong());
                })
                .toList();
    }
}
