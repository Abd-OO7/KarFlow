package ma.karflow.feature.billing.dto;

import ma.karflow.feature.billing.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        BigDecimal amount,
        LocalDateTime paymentDate,
        PaymentMethod paymentMethod,
        String transactionRef,
        String notes,
        UUID invoiceId,
        LocalDateTime createdAt
) {
}
