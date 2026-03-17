package ma.karflow.feature.rental.dto;

import ma.karflow.feature.rental.enums.RentalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate actualReturnDate,
        Double mileageBefore,
        Double mileageAfter,
        double deposit,
        double totalAmount,
        RentalStatus status,
        String notes,
        UUID vehicleId,
        String vehicleLicensePlate,
        String vehicleModelName,
        UUID clientId,
        String clientFullName,
        UUID insuranceId,
        String insuranceName,
        LocalDateTime createdAt
) {
}
