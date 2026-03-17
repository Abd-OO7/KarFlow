package ma.karflow.feature.vehicle.dto;

import ma.karflow.feature.vehicle.enums.FuelType;
import ma.karflow.feature.vehicle.enums.TransmissionType;

import java.util.UUID;

public record VehicleModelResponse(
        UUID id,
        String name,
        Integer horsePower,
        Integer doorCount,
        Integer seatCount,
        Integer trunkVolume,
        FuelType fuelType,
        TransmissionType transmissionType,
        Integer year,
        BrandResponse brand
) {
}
