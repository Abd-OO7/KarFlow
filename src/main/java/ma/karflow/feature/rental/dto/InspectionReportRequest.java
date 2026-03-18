package ma.karflow.feature.rental.dto;

import jakarta.validation.constraints.NotNull;
import ma.karflow.feature.rental.enums.InspectionType;

public record InspectionReportRequest(
        @NotNull(message = "Le type d'inspection est obligatoire")
        InspectionType type,

        Double fuelLevel,
        Double mileage,

        boolean exteriorFrontScratches,
        boolean exteriorFrontDents,
        boolean exteriorRearScratches,
        boolean exteriorRearDents,
        boolean exteriorLeftScratches,
        boolean exteriorLeftDents,
        boolean exteriorRightScratches,
        boolean exteriorRightDents,

        String interiorCondition,
        String tireCondition,
        String comments,
        String photos,
        String clientSignature,
        String agentSignature
) {
}
