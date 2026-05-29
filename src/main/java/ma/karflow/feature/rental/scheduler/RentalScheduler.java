package ma.karflow.feature.rental.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.billing.entity.Invoice;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.feature.billing.repository.InvoiceRepository;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.repository.RentalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalScheduler {

    private final RentalRepository rentalRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Every hour: mark overdue rentals.
     * Finds ALL active rentals (across all tenants) where endDate < today.
     * This is a system-level operation, not tenant-scoped.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void markOverdueRentals() {
        List<Rental> overdue = rentalRepository.findAllOverdueRentals(LocalDate.now());
        for (Rental rental : overdue) {
            rental.setStatus(RentalStatus.OVERDUE);
            log.info("Rental {} marked as OVERDUE (tenant: {})", rental.getId(), rental.getTenantId());
        }
        rentalRepository.saveAll(overdue);
        if (!overdue.isEmpty()) {
            log.info("Marked {} rentals as OVERDUE", overdue.size());
        }
    }

    /**
     * Every hour: mark overdue invoices.
     * Finds invoices across all tenants where status = SENT and dueDate < today.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void markOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findAllOverdueInvoices(LocalDate.now());
        for (Invoice invoice : overdue) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            log.info("Invoice {} marked as OVERDUE (tenant: {})", invoice.getId(), invoice.getTenantId());
        }
        invoiceRepository.saveAll(overdue);
        if (!overdue.isEmpty()) {
            log.info("Marked {} invoices as OVERDUE", overdue.size());
        }
    }
}
