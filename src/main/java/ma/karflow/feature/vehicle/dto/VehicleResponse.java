package ma.karflow.feature.vehicle.dto;

import ma.karflow.feature.vehicle.enums.VehicleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String licensePlate,
        String color,
        BigDecimal mileage,
        BigDecimal dailyRate,
        String photoUrl,
        VehicleStatus status,
        VehicleModelResponse vehicleModel,
        CategoryResponse category,
        LocalDateTime createdAt
) {
}
