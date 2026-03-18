package ma.karflow.feature.dashboard.dto;

public record RevenueDataPoint(
        String period,
        double amount,
        long count
) {
}
