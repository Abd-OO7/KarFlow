package ma.karflow.feature.rental.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReturnRequest(
        @NotNull(message = "La date de retour est obligatoire")
        LocalDate actualReturnDate,

        @NotNull(message = "La fiche d'état retour est obligatoire")
        @Valid
        InspectionReportRequest inspectionReport
) {
}
