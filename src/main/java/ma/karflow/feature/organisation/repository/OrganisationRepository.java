package ma.karflow.feature.organisation.repository;

import ma.karflow.feature.organisation.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Optional<Organisation> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Organisation> findByTenantId(UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);
}
