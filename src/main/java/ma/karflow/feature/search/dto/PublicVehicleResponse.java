package ma.karflow.feature.search.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicVehicleResponse(
        UUID id,
        String photoUrl,
        String brandName,
        String modelName,
        String categoryName,
        int seatCount,
        int doorCount,
        String fuelType,
        String transmissionType,
        int horsePower,
        int year,
        BigDecimal dailyRate,
        BigDecimal totalPrice,
        String agencyName,
        String agencyLogoUrl
) {
}
