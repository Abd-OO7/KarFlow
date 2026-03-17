package ma.karflow.feature.client.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClientResponse(
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
        String photoUrl,
        boolean licenseValid,
        LocalDateTime createdAt
) {
}
