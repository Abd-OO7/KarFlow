package ma.karflow.feature.rental.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.UUID;

public record RentalCreateRequest(
        @NotNull(message = "La date de début est obligatoire")
        @FutureOrPresent(message = "La date de début ne peut pas être dans le passé")
        LocalDate startDate,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDate endDate,

        @PositiveOrZero(message = "La caution doit être positive ou nulle")
        double deposit,

        String notes,

        @NotNull(message = "Le véhicule est obligatoire")
        UUID vehicleId,

        @NotNull(message = "Le client est obligatoire")
        UUID clientId,

        UUID insuranceId
) {
}
