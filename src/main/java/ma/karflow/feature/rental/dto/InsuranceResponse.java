package ma.karflow.feature.rental.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InsuranceResponse(
        UUID id,
        String name,
        String description,
        String coverageType,
        BigDecimal dailyRate,
        String provider
) {
}
