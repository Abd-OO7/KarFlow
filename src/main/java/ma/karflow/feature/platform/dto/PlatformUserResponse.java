package ma.karflow.feature.platform.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record PlatformUserResponse(
        UUID id,
        String username,
        String email,
        String phone,
        boolean enabled,
        String organisationName,
        Set<String> roles,
        LocalDateTime createdAt
) {}
