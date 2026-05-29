package ma.karflow.feature.billing.repository;

import ma.karflow.feature.billing.entity.Invoice;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    BigDecimal sumPaidAmountByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status = 'PAID' AND i.paidDate BETWEEN :from AND :to")
    BigDecimal sumPaidAmountByTenantIdAndDateRange(@Param("tenantId") UUID tenantId,
                                                    @Param("from") java.time.LocalDate from,
                                                    @Param("to") java.time.LocalDate to);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status = 'PAID' AND i.paidDate BETWEEN :from AND :to")
    long countPaidByTenantIdAndDateRange(@Param("tenantId") UUID tenantId,
                                         @Param("from") java.time.LocalDate from,
                                         @Param("to") java.time.LocalDate to);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status IN ('SENT', 'OVERDUE', 'PAID') AND i.deleted = false")
    long countAllNonDraftByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status IN ('SENT', 'OVERDUE') AND i.deleted = false")
    long countUnpaidByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' AND i.dueDate < :today AND i.deleted = false")
    List<Invoice> findAllOverdueInvoices(@Param("today") LocalDate today);
}
