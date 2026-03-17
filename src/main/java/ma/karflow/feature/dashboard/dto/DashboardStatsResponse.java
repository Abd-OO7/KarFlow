package ma.karflow.feature.dashboard.dto;

import java.util.List;

public record DashboardStatsResponse(
        long totalVehicles,
        long availableVehicles,
        long rentedVehicles,
        long maintenanceVehicles,
        long totalClients,
        long activeRentals,
        long overdueRentals,
        long totalInvoices,
        long unpaidInvoices,
        double totalRevenue,
        double fleetOccupancyRate,
        List<UpcomingReturnDto> upcomingReturns
) {
}
