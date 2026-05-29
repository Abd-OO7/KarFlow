package ma.karflow.feature.billing.dto;

import ma.karflow.feature.billing.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal discount,
        BigDecimal totalAmount,
        BigDecimal totalPaid,
        BigDecimal remainingAmount,
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
