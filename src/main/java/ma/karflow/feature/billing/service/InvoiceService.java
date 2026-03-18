package ma.karflow.feature.billing.service;

import ma.karflow.feature.billing.dto.InvoiceResponse;
import ma.karflow.feature.billing.dto.PaymentRequest;
import ma.karflow.feature.billing.dto.PaymentResponse;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InvoiceService {
    PageResponse<InvoiceResponse> getAll(Pageable pageable);
    PageResponse<InvoiceResponse> getByStatus(InvoiceStatus status, Pageable pageable);
    InvoiceResponse getById(UUID id);
    InvoiceResponse generateForRental(UUID rentalId);
    PaymentResponse addPayment(UUID invoiceId, PaymentRequest request);
    PageResponse<PaymentResponse> getPayments(UUID invoiceId, Pageable pageable);
    byte[] generatePdf(UUID invoiceId);
    void sendByEmail(UUID invoiceId);
}
