package ma.karflow.feature.billing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.billing.dto.InvoiceResponse;
import ma.karflow.feature.billing.dto.PaymentRequest;
import ma.karflow.feature.billing.dto.PaymentResponse;
import ma.karflow.feature.billing.entity.Invoice;
import ma.karflow.feature.billing.entity.InvoiceLine;
import ma.karflow.feature.billing.entity.Payment;
import ma.karflow.feature.billing.enums.InvoiceLineType;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.feature.billing.mapper.InvoiceMapper;
import ma.karflow.feature.billing.mapper.PaymentMapper;
import ma.karflow.feature.billing.repository.InvoiceRepository;
import ma.karflow.feature.billing.repository.PaymentRepository;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.EmailService;
import ma.karflow.shared.util.PdfGenerator;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final InvoiceMapper invoiceMapper;
    private final PaymentMapper paymentMapper;
    private final PdfGenerator pdfGenerator;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAll(Pageable pageable) {
        return PageResponse.from(invoiceRepository.findByTenantId(TenantContext.getTenantId(), pageable), invoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getByStatus(InvoiceStatus status, Pageable pageable) {
        return PageResponse.from(invoiceRepository.findByTenantIdAndStatus(TenantContext.getTenantId(), status, pageable), invoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {
        return invoiceMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public InvoiceResponse generateForRental(UUID rentalId) {
        UUID tenantId = TenantContext.getTenantId();

        // Check if invoice already exists for this rental
        Optional<Invoice> existing = invoiceRepository.findByRentalIdAndTenantId(rentalId, tenantId);
        if (existing.isPresent()) {
            return invoiceMapper.toResponse(existing.get());
        }

        Rental rental = rentalRepository.findByIdAndTenantId(rentalId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));

        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setInvoiceNumber(generateInvoiceNumber(tenantId));
        invoice.setRental(rental);
        invoice.setDueDate(LocalDate.now().plusDays(30));

        // Build invoice lines
        long days = ChronoUnit.DAYS.between(rental.getStartDate(),
                rental.getActualReturnDate() != null ? rental.getActualReturnDate() : rental.getEndDate());
        if (days < 1) days = 1;

        double dailyRate = rental.getVehicle().getDailyRate() * rental.getVehicle().getCategory().getDailyRateMultiplier();

        // Line: rental days
        InvoiceLine rentalLine = new InvoiceLine(
                "Location véhicule " + rental.getVehicle().getLicensePlate() + " — " + days + " jour(s)",
                days, dailyRate, InvoiceLineType.RENTAL_DAYS);
        rentalLine.setTenantId(tenantId);
        rentalLine.setInvoice(invoice);
        invoice.getLines().add(rentalLine);

        // Line: insurance
        if (rental.getInsurance() != null) {
            InvoiceLine insuranceLine = new InvoiceLine(
                    "Assurance " + rental.getInsurance().getName(),
                    days, rental.getInsurance().getDailyRate(), InvoiceLineType.INSURANCE);
            insuranceLine.setTenantId(tenantId);
            insuranceLine.setInvoice(invoice);
            invoice.getLines().add(insuranceLine);
        }

        // Line: late return
        if (rental.getActualReturnDate() != null && rental.getActualReturnDate().isAfter(rental.getEndDate())) {
            long extraDays = ChronoUnit.DAYS.between(rental.getEndDate(), rental.getActualReturnDate());
            InvoiceLine lateLine = new InvoiceLine(
                    "Retour tardif — " + extraDays + " jour(s) supplémentaire(s) (+50%)",
                    extraDays, dailyRate * 1.5, InvoiceLineType.LATE_RETURN);
            lateLine.setTenantId(tenantId);
            lateLine.setInvoice(invoice);
            invoice.getLines().add(lateLine);
        }

        // Line: extra km
        if (rental.getMileageBefore() != null && rental.getMileageAfter() != null) {
            double kmDriven = rental.getMileageAfter() - rental.getMileageBefore();
            double freeKm = days * 250;
            if (kmDriven > freeKm) {
                double extraKm = kmDriven - freeKm;
                InvoiceLine kmLine = new InvoiceLine(
                        "Kilomètres supplémentaires — " + String.format("%.0f", extraKm) + " km",
                        extraKm, 2.0, InvoiceLineType.EXTRA_KM);
                kmLine.setTenantId(tenantId);
                kmLine.setInvoice(invoice);
                invoice.getLines().add(kmLine);
            }
        }

        invoice.recalculate();
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice = invoiceRepository.save(invoice);

        log.info("Invoice {} generated for rental {} — total: {} MAD (tenantId: {})",
                invoice.getInvoiceNumber(), rentalId, invoice.getTotalAmount(), tenantId);
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public PaymentResponse addPayment(UUID invoiceId, PaymentRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Invoice invoice = findByIdOrThrow(invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Cette facture est déjà entièrement payée");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Impossible d'ajouter un paiement à une facture annulée");
        }

        double remaining = invoice.getTotalAmount() - invoice.getTotalPaid();
        if (request.amount() > remaining + 0.01) {
            throw new BusinessException("Le montant du paiement (" + request.amount()
                    + " MAD) dépasse le restant dû (" + String.format("%.2f", remaining) + " MAD)");
        }

        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setAmount(request.amount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setTransactionRef(request.transactionRef());
        payment.setNotes(request.notes());
        payment.setInvoice(invoice);
        payment = paymentRepository.save(payment);

        // Update invoice status if fully paid
        double totalPaid = invoice.getTotalPaid() + request.amount();
        if (totalPaid >= invoice.getTotalAmount() - 0.01) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidDate(LocalDate.now());

            // Mark rental as RETURNED if still ACTIVE
            Rental rental = invoice.getRental();
            if (rental.getStatus() == RentalStatus.ACTIVE || rental.getStatus() == RentalStatus.OVERDUE) {
                rental.setStatus(RentalStatus.RETURNED);
                rentalRepository.save(rental);
            }
        } else if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.SENT);
        }
        invoiceRepository.save(invoice);

        log.info("Payment {} MAD added to invoice {} (tenantId: {})", request.amount(), invoice.getInvoiceNumber(), tenantId);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPayments(UUID invoiceId, Pageable pageable) {
        findByIdOrThrow(invoiceId);
        return PageResponse.from(
                paymentRepository.findByInvoiceIdAndInvoiceTenantId(invoiceId, TenantContext.getTenantId(), pageable),
                paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(UUID invoiceId) {
        Invoice invoice = findByIdOrThrow(invoiceId);

        List<String> headers = List.of("Description", "Quantité", "Prix unitaire", "Total");
        List<List<String>> rows = invoice.getLines().stream()
                .map(line -> List.of(
                        line.getLabel(),
                        String.format("%.0f", line.getQuantity()),
                        String.format("%.2f MAD", line.getUnitPrice()),
                        String.format("%.2f MAD", line.getTotalPrice())
                ))
                .collect(java.util.stream.Collectors.toList());

        // Add totals
        rows.add(List.of("", "", "Sous-total HT", String.format("%.2f MAD", invoice.getSubtotal())));
        rows.add(List.of("", "", "TVA (" + (int) invoice.getTaxRate() + "%)", String.format("%.2f MAD", invoice.getTaxAmount())));
        if (invoice.getDiscount() > 0) {
            rows.add(List.of("", "", "Remise", String.format("-%.2f MAD", invoice.getDiscount())));
        }
        rows.add(List.of("", "", "TOTAL TTC", String.format("%.2f MAD", invoice.getTotalAmount())));

        String title = "Facture " + invoice.getInvoiceNumber()
                + "\nClient : " + invoice.getRental().getClient().getFirstName() + " " + invoice.getRental().getClient().getLastName()
                + "\nVéhicule : " + invoice.getRental().getVehicle().getLicensePlate();

        return pdfGenerator.generateTablePdf(title, headers, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public void sendByEmail(UUID invoiceId) {
        Invoice invoice = findByIdOrThrow(invoiceId);
        String clientEmail = invoice.getRental().getClient().getEmail();

        if (clientEmail == null || clientEmail.isBlank()) {
            throw new BusinessException("Le client n'a pas d'adresse email configurée");
        }

        byte[] pdf = generatePdf(invoiceId);

        emailService.sendHtmlEmailWithPdf(
                clientEmail,
                "KarFlow — Facture " + invoice.getInvoiceNumber(),
                "invoice",
                Map.of(
                        "invoiceNumber", invoice.getInvoiceNumber(),
                        "clientName", invoice.getRental().getClient().getFirstName(),
                        "totalAmount", String.format("%.2f", invoice.getTotalAmount()),
                        "dueDate", invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A"
                ),
                pdf,
                "facture-" + invoice.getInvoiceNumber() + ".pdf"
        );

        // Update status to SENT if still DRAFT
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.SENT);
            invoiceRepository.save(invoice);
        }

        log.info("Invoice {} sent by email to {} (tenantId: {})", invoice.getInvoiceNumber(), clientEmail, invoice.getTenantId());
    }

    private Invoice findByIdOrThrow(UUID id) {
        return invoiceRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    private String generateInvoiceNumber(UUID tenantId) {
        int year = LocalDate.now().getYear();
        String prefix = "INV-" + year + "-";
        int maxSeq = invoiceRepository.findMaxSequenceByPrefix(tenantId, prefix + "%");
        return prefix + String.format("%05d", maxSeq + 1);
    }
}
