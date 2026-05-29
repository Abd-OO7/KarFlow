package ma.karflow.feature.platform.dto;

public record PlatformStatsResponse(
        long totalOrganisations,
        long totalUsers,
        long totalVehicles,
        long totalClients,
        long totalRentals,
        long totalInvoices
) {}
