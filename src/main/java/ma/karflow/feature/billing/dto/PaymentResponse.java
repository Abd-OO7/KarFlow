package ma.karflow.feature.billing.dto;

import ma.karflow.feature.billing.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        double amount,
        LocalDateTime paymentDate,
        PaymentMethod paymentMethod,
        String transactionRef,
        String notes,
        UUID invoiceId,
        LocalDateTime createdAt
) {
}
