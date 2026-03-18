package ma.karflow.feature.organisation.repository;

import ma.karflow.feature.organisation.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {

    Page<City> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<City> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);

    @Query("SELECT c FROM City c WHERE c.tenantId = :tenantId AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<City> searchByName(@Param("tenantId") UUID tenantId, @Param("query") String query, Pageable pageable);
}
