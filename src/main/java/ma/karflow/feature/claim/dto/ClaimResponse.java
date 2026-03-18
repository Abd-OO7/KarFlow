package ma.karflow.feature.claim.dto;

import ma.karflow.feature.claim.enums.ClaimPriority;
import ma.karflow.feature.claim.enums.ClaimStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID id,
        String subject,
        String description,
        ClaimStatus status,
        ClaimPriority priority,
        String resolution,
        LocalDateTime resolvedAt,
        UUID clientId,
        String clientFullName,
        UUID rentalId,
        LocalDateTime createdAt
) {
}
