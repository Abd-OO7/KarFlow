package ma.karflow.feature.platform.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlatformOrgResponse(
        UUID id,
        UUID tenantId,
        String name,
        String email,
        String phone,
        String plan,
        String subscriptionStatus,
        LocalDateTime createdAt,
        long userCount,
        long vehicleCount
) {}
