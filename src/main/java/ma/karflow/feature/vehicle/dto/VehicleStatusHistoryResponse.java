package ma.karflow.feature.vehicle.dto;

import ma.karflow.feature.vehicle.enums.VehicleStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleStatusHistoryResponse(
        UUID id,
        VehicleStatus previousStatus,
        VehicleStatus newStatus,
        LocalDateTime changeDate,
        String comment
) {
}
