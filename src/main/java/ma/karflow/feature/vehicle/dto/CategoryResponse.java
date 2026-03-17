package ma.karflow.feature.vehicle.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        double dailyRateMultiplier
) {
}
