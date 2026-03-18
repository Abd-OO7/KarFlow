package ma.karflow.feature.organisation.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record OrganisationResponse(
        UUID id,
        String name,
        String siret,
        String phone,
        String email,
        String logoUrl,
        String address,
        String subscriptionPlan,
        Set<CityResponse> cities,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
