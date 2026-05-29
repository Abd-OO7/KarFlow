package ma.karflow.feature.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleRequest(
        @NotBlank(message = "La plaque d'immatriculation est obligatoire")
        @Size(max = 20)
        String licensePlate,

        @Size(max = 50)
        String color,

        @NotNull(message = "Le kilométrage est obligatoire")
        BigDecimal mileage,

        @Positive(message = "Le tarif journalier doit être positif")
        BigDecimal dailyRate,

        String photoUrl,

        @NotNull(message = "Le modèle est obligatoire")
        UUID vehicleModelId,

        @NotNull(message = "La catégorie est obligatoire")
        UUID categoryId
) {
}
