package ma.karflow.feature.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String phone,
        String photoUrl,
        boolean enabled,
        UUID organisationId,
        Set<String> roles,
        LocalDateTime createdAt
) {
}
