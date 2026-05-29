package ma.karflow.feature.search.dto;

import java.util.List;
import java.util.UUID;

public record PublicAgencyResponse(
        UUID id,
        String name,
        String logoUrl,
        String address,
        String phone,
        String email,
        List<String> cityNames,
        long vehicleCount,
        double rating,
        String subscriptionPlan
) {
}
