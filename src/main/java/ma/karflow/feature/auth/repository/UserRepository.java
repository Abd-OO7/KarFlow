package ma.karflow.feature.auth.repository;

import ma.karflow.feature.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);
}
