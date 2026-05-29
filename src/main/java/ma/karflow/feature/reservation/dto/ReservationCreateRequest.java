package ma.karflow.feature.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationCreateRequest(
        @NotNull(message = "Le véhicule est obligatoire")
        UUID vehicleId,

        @NotNull(message = "La date de début est obligatoire")
        @FutureOrPresent(message = "La date de début ne peut pas être dans le passé")
        LocalDate startDate,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDate endDate,

        UUID insuranceId,

        String notes,

        @NotBlank(message = "Le prénom du client est obligatoire")
        String clientFirstName,

        @NotBlank(message = "Le nom du client est obligatoire")
        String clientLastName,

        @NotBlank(message = "L'email du client est obligatoire")
        @Email(message = "L'email du client n'est pas valide")
        String clientEmail,

        String clientPhone,

        String clientCin
) {
}
