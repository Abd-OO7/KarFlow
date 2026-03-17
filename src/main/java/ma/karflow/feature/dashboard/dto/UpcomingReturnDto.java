package ma.karflow.feature.dashboard.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpcomingReturnDto(
        UUID rentalId,
        String clientFullName,
        String vehicleLicensePlate,
        String vehicleModelName,
        LocalDate endDate,
        boolean overdue
) {
}
