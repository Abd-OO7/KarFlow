package ma.karflow.feature.billing.repository;

import ma.karflow.feature.billing.entity.Invoice;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Invoice> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Invoice> findByRentalIdAndTenantId(UUID rentalId, UUID tenantId);

    Page<Invoice> findByTenantIdAndStatus(UUID tenantId, InvoiceStatus status, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNumber, 10) AS int)), 0) FROM Invoice i WHERE i.tenantId = :tenantId AND i.invoiceNumber LIKE :prefix")
    int findMaxSequenceByPrefix(@Param("tenantId") UUID tenantId, @Param("prefix") String prefix);

    long countByTenantIdAndStatus(UUID tenantId, InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status = 'PAID'")
    double sumPaidAmountByTenantId(@Param("tenantId") UUID tenantId);
}
