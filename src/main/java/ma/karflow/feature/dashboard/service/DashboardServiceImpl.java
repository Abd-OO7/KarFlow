package ma.karflow.feature.dashboard.service;

import lombok.RequiredArgsConstructor;
import ma.karflow.feature.billing.repository.InvoiceRepository;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.feature.dashboard.dto.DashboardStatsResponse;
import ma.karflow.feature.dashboard.dto.RevenueDataPoint;
import ma.karflow.feature.dashboard.dto.UpcomingReturnDto;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.shared.util.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final RentalRepository rentalRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        UUID tenantId = TenantContext.getTenantId();
        LocalDate today = LocalDate.now();

        // Vehicle counts — single queries each
        long totalVehicles = vehicleRepository.countAllByTenantId(tenantId);
        long available = vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.AVAILABLE);
        long rented = vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.RENTED);
        long maintenance = vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.MAINTENANCE)
                + vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.GARAGE);

        // Client count
        long totalClients = clientRepository.countByTenantId(tenantId);

        // Rental counts
        long activeRentals = rentalRepository.countByTenantIdAndStatus(tenantId, RentalStatus.ACTIVE);
        long overdueRentals = rentalRepository.findOverdueRentals(tenantId, today).size();

        // Invoice counts — optimized single queries
        long totalInvoices = invoiceRepository.countAllNonDraftByTenantId(tenantId);
        long unpaidInvoices = invoiceRepository.countUnpaidByTenantId(tenantId);

        // Revenue — single aggregate query
        BigDecimal totalRevenue = invoiceRepository.sumPaidAmountByTenantId(tenantId);

        // Occupancy rate
        double occupancyRate = totalVehicles > 0 ? (double) rented / totalVehicles * 100 : 0;

        // Upcoming returns — single JOIN FETCH query (no N+1)
        List<UpcomingReturnDto> upcomingReturns = rentalRepository
                .findUpcomingReturns(tenantId, today.plusDays(7))
                .stream()
                .map(r -> new UpcomingReturnDto(
                        r.getId(),
                        r.getClient().getFirstName() + " " + r.getClient().getLastName(),
                        r.getVehicle().getLicensePlate(),
                        r.getVehicle().getVehicleModel().getName(),
                        r.getEndDate(),
                        r.getEndDate().isBefore(today)
                ))
                .toList();

        return new DashboardStatsResponse(
                totalVehicles, available, rented, maintenance,
                totalClients, activeRentals, overdueRentals,
                totalInvoices, unpaidInvoices,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                Math.round(occupancyRate * 100.0) / 100.0,
                upcomingReturns
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueDataPoint> getRevenue(LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.getTenantId();

        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        LocalDate current = from.withDayOfMonth(1);

        while (!current.isAfter(to)) {
            LocalDate monthEnd = current.plusMonths(1).minusDays(1);
            if (monthEnd.isAfter(to)) monthEnd = to;

            // Single aggregate query per month instead of loading all invoices
            BigDecimal monthRevenue = invoiceRepository.sumPaidAmountByTenantIdAndDateRange(tenantId, current, monthEnd);
            long monthCount = invoiceRepository.countPaidByTenantIdAndDateRange(tenantId, current, monthEnd);

            dataPoints.add(new RevenueDataPoint(
                    current.getMonth().name() + " " + current.getYear(),
                    monthRevenue != null ? monthRevenue : BigDecimal.ZERO,
                    monthCount
            ));

            current = current.plusMonths(1);
        }

        return dataPoints;
    }
}
