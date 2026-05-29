package ma.karflow.feature.search.dto;

import java.util.UUID;

public record CitySearchResponse(
        UUID id,
        String name,
        String region,
        long agencyCount
) {
}
