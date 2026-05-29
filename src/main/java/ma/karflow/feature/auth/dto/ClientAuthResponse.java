package ma.karflow.feature.auth.dto;

import java.util.UUID;

public record ClientAuthResponse(
        String accessToken,
        String refreshToken,
        UUID clientId,
        String email,
        String firstName,
        String lastName,
        String role
) {
}
