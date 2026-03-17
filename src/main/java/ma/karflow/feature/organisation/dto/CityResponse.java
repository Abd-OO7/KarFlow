package ma.karflow.feature.organisation.dto;

import java.util.UUID;

public record CityResponse(
        UUID id,
        String name,
        String region,
        String country,
        Double latitude,
        Double longitude
) {
}
