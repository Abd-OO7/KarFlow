package ma.karflow.feature.vehicle.dto;

import jakarta.validation.constraints.NotNull;
import ma.karflow.feature.vehicle.enums.VehicleStatus;

public record VehicleStatusRequest(
        @NotNull(message = "Le nouveau statut est obligatoire")
        VehicleStatus status,

        String comment
) {
}
