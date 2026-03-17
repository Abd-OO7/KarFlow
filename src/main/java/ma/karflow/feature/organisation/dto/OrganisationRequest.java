package ma.karflow.feature.organisation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganisationRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
        String name,

        @Size(max = 20, message = "Le SIRET ne peut pas dépasser 20 caractères")
        String siret,

        @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
        String phone,

        @Email(message = "Format d'email invalide")
        String email,

        String logoUrl,

        @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
        String address
) {
}
