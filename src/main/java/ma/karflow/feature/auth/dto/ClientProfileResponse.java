package ma.karflow.feature.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClientProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String cin,
        String address,
        String licenseNumber,
        LocalDate licenseExpiry,
        LocalDate dateOfBirth,
        String photoUrl
) {
}
