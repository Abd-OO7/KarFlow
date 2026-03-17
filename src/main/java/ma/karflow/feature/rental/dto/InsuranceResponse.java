package ma.karflow.feature.rental.dto;

import java.util.UUID;

public record InsuranceResponse(
        UUID id,
        String name,
        String description,
        String coverageType,
        double dailyRate,
        String provider
) {
}
