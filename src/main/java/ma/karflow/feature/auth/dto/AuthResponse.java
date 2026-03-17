package ma.karflow.feature.auth.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String username,
        String email,
        UUID organisationId,
        Set<String> roles,
        Set<String> permissions
) {
}
