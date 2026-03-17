package ma.karflow.feature.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100)
        String name,

        @Size(max = 100)
        String slug,

        String logoUrl
) {
}
