package ma.karflow.feature.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ma.karflow.feature.claim.enums.ClaimPriority;

import java.util.UUID;

public record ClaimRequest(
        @NotBlank(message = "Le sujet est obligatoire")
        String subject,

        String description,

        ClaimPriority priority,

        @NotNull(message = "Le client est obligatoire")
        UUID clientId,

        UUID rentalId
) {
}
