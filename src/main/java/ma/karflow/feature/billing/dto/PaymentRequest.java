package ma.karflow.feature.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ma.karflow.feature.billing.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        @Positive(message = "Le montant doit être positif")
        BigDecimal amount,

        @NotNull(message = "La méthode de paiement est obligatoire")
        PaymentMethod paymentMethod,

        String transactionRef,

        String notes
) {
}
