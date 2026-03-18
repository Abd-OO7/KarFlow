package ma.karflow.feature.billing.repository;

import ma.karflow.feature.billing.entity.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID> {
}
