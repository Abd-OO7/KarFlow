package ma.karflow.feature.billing.dto;

import ma.karflow.feature.billing.enums.InvoiceLineType;

import java.util.UUID;

public record InvoiceLineResponse(
        UUID id,
        String label,
        double quantity,
        double unitPrice,
        double totalPrice,
        InvoiceLineType lineType
) {
}
