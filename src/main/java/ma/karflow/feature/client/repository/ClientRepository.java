package ma.karflow.feature.client.repository;

import ma.karflow.feature.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Page<Client> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Client> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByCinAndTenantId(String cin, UUID tenantId);

    @Query("SELECT c FROM Client c WHERE c.tenantId = :tenantId AND (" +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "c.cin LIKE CONCAT('%', :q, '%') OR " +
            "c.phone LIKE CONCAT('%', :q, '%'))")
    Page<Client> search(@Param("tenantId") UUID tenantId, @Param("q") String query, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
