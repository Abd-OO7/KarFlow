package ma.karflow.feature.dashboard.dto;

import java.math.BigDecimal;

public record RevenueDataPoint(
        String period,
        BigDecimal amount,
        long count
) {
}
