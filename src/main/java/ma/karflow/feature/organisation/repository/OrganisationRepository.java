package ma.karflow.feature.organisation.repository;

import ma.karflow.feature.organisation.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Optional<Organisation> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Organisation> findByTenantId(UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);

    @Query("SELECT o FROM Organisation o JOIN o.cities c WHERE c.id = :cityId AND o.deleted = false")
    List<Organisation> findByCityId(@Param("cityId") UUID cityId);

    @Query("SELECT o.tenantId FROM Organisation o JOIN o.cities c WHERE c.id = :cityId AND o.deleted = false")
    List<UUID> findTenantIdsByCityId(@Param("cityId") UUID cityId);

    @Query("SELECT COUNT(o) FROM Organisation o JOIN o.cities c WHERE c.id = :cityId AND o.deleted = false")
    long countByCityId(@Param("cityId") UUID cityId);

    @Query("SELECT o FROM Organisation o LEFT JOIN FETCH o.cities WHERE o.tenantId IN :tenantIds AND o.deleted = false")
    List<Organisation> findByTenantIdIn(@Param("tenantIds") List<UUID> tenantIds);

    @Query("SELECT o FROM Organisation o LEFT JOIN FETCH o.cities WHERE o.id = :id AND o.deleted = false")
    Optional<Organisation> findByIdWithCities(@Param("id") UUID id);
}
