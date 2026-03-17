package ma.karflow.feature.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClientRequest(
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100)
        String lastName,

        @Email(message = "Format d'email invalide")
        String email,

        @Size(max = 20)
        String phone,

        @Size(max = 30)
        String cin,

        @Size(max = 500)
        String address,

        @Size(max = 50)
        String licenseNumber,

        LocalDate licenseExpiry,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateOfBirth,

        String photoUrl
) {
}
