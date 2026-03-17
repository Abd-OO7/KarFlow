package ma.karflow.feature.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ma.karflow.feature.vehicle.enums.FuelType;
import ma.karflow.feature.vehicle.enums.TransmissionType;

import java.util.UUID;

public record VehicleModelRequest(
        @NotBlank(message = "Le nom du modèle est obligatoire")
        @Size(max = 100)
        String name,

        Integer horsePower,
        Integer doorCount,
        Integer seatCount,
        Integer trunkVolume,
        FuelType fuelType,
        TransmissionType transmissionType,
        Integer year,

        @NotNull(message = "La marque est obligatoire")
        UUID brandId
) {
}
