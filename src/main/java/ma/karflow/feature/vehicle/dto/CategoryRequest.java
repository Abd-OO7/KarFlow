package ma.karflow.feature.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Positive(message = "Le multiplicateur doit être positif")
        Double dailyRateMultiplier
) {
}
