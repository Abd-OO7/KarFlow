package ma.karflow.feature.rental.dto;

import ma.karflow.feature.rental.enums.InspectionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record InspectionReportResponse(
        UUID id,
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
        String agentSignature,
        UUID rentalId,
        LocalDateTime createdAt
) {
}
