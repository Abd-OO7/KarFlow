package ma.karflow.feature.search.dto;

import java.util.List;
import java.util.UUID;

public record PublicAgencyDetailResponse(
        UUID id,
        String name,
        String logoUrl,
        String address,
        String phone,
        String email,
        String siret,
        List<CityInfo> cities,
        long vehicleCount,
        double rating,
        String subscriptionPlan,
        String description
) {

    public record CityInfo(
            String name,
            String region
    ) {
    }
}
