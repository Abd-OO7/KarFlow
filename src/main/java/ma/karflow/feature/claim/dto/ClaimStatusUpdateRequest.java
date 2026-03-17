package ma.karflow.feature.claim.dto;

import jakarta.validation.constraints.NotNull;
import ma.karflow.feature.claim.enums.ClaimStatus;

public record ClaimStatusUpdateRequest(
        @NotNull(message = "Le statut est obligatoire")
        ClaimStatus status,

        String resolution
) {
}
