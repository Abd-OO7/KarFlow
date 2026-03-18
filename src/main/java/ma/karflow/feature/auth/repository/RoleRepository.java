package ma.karflow.feature.auth.repository;

import ma.karflow.feature.auth.entity.Role;
import ma.karflow.feature.auth.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndTenantId(RoleType name, UUID tenantId);
}
