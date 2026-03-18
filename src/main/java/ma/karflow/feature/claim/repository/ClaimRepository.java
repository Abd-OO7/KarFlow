package ma.karflow.feature.claim.repository;

import ma.karflow.feature.claim.entity.Claim;
import ma.karflow.feature.claim.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    Page<Claim> findByTenantId(UUID tenantId, Pageable pageable);
    Optional<Claim> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<Claim> findByTenantIdAndStatus(UUID tenantId, ClaimStatus status, Pageable pageable);
    Page<Claim> findByClientIdAndTenantId(UUID clientId, UUID tenantId, Pageable pageable);
}
