package ma.karflow.feature.billing.repository;

import ma.karflow.feature.billing.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByInvoiceIdAndInvoiceTenantId(UUID invoiceId, UUID tenantId, Pageable pageable);
}
