package ma.karflow.feature.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record InsuranceRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String name,
        String description,
        String coverageType,
        @Positive(message = "Le tarif journalier doit être positif")
        double dailyRate,
        String provider
) {
}
