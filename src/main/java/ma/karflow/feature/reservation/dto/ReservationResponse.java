package ma.karflow.feature.reservation.dto;

import ma.karflow.feature.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID clientId,
        String clientFullName,
        String clientEmail,
        UUID vehicleId,
        String vehicleInfo,
        UUID insuranceId,
        String insuranceName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal estimatedTotal,
        BigDecimal depositRequired,
        ReservationStatus status,
        String notes,
        String cancellationReason,
        LocalDateTime cancelledAt,
        LocalDateTime confirmedAt,
        LocalDateTime convertedAt,
        UUID rentalId,
        LocalDateTime createdAt
) {
}
