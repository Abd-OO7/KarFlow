package ma.karflow.feature.billing.dto;

import ma.karflow.feature.billing.enums.InvoiceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        double subtotal,
        double taxRate,
        double taxAmount,
        double discount,
        double totalAmount,
        double totalPaid,
        double remainingAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDate paidDate,
        UUID rentalId,
        String clientFullName,
        String vehicleLicensePlate,
        List<InvoiceLineResponse> lines,
        LocalDateTime createdAt
) {
}
