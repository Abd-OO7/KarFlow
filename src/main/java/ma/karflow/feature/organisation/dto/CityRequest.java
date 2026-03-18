package ma.karflow.feature.organisation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CityRequest(
        @NotBlank(message = "Le nom de la ville est obligatoire")
        @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
        String name,

        String region,

        String country,

        Double latitude,

        Double longitude
) {
}
