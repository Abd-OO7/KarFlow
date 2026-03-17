package ma.karflow.feature.rental.repository;

import ma.karflow.feature.rental.entity.InspectionReport;
import ma.karflow.feature.rental.enums.InspectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InspectionReportRepository extends JpaRepository<InspectionReport, UUID> {

    Optional<InspectionReport> findByRentalIdAndTypeAndTenantId(UUID rentalId, InspectionType type, UUID tenantId);

    boolean existsByRentalIdAndTypeAndTenantId(UUID rentalId, InspectionType type, UUID tenantId);
}
