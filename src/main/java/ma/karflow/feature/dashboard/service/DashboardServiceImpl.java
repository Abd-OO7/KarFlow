package ma.karflow.feature.dashboard.service;

import lombok.RequiredArgsConstructor;
import ma.karflow.feature.billing.enums.InvoiceStatus;
import ma.karflow.feature.billing.repository.InvoiceRepository;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.feature.dashboard.dto.DashboardStatsResponse;
import ma.karflow.feature.dashboard.dto.RevenueDataPoint;
import ma.karflow.feature.dashboard.dto.UpcomingReturnDto;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        long totalVehicles = vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.AVAILABLE)
                + vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.RENTED)
                + vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.MAINTENANCE)
                + vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.GARAGE);
        long available = vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.AVAILABLE);
        long rented = vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.RENTED);
        long maintenance = vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.MAINTENANCE)
                + vehicleRepository.countByTenantIdAndStatus(tenantId, VehicleStatus.GARAGE);

        long totalClients = clientRepository.countByTenantId(tenantId);

        long activeRentals = rentalRepository.countByTenantIdAndStatus(tenantId, RentalStatus.ACTIVE);
        List<Rental> overdueList = rentalRepository.findOverdueRentals(tenantId, LocalDate.now());
        long overdueRentals = overdueList.size();

        long totalInvoices = invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.PAID)
                + invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.SENT)
                + invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.OVERDUE);
        long unpaidInvoices = invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.SENT)
                + invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.OVERDUE);

        double totalRevenue = invoiceRepository.sumPaidAmountByTenantId(tenantId);

        double occupancyRate = totalVehicles > 0 ? (double) rented / totalVehicles * 100 : 0;

        // Upcoming returns: active rentals ending in the next 7 days + overdue
        List<UpcomingReturnDto> upcomingReturns = new ArrayList<>();
        rentalRepository.findByTenantIdAndStatus(tenantId, RentalStatus.ACTIVE, PageRequest.of(0, 20))
                .getContent()
                .stream()
                .filter(r -> r.getEndDate() != null && r.getEndDate().isBefore(LocalDate.now().plusDays(7)))
                .forEach(r -> upcomingReturns.add(new UpcomingReturnDto(
                        r.getId(),
                        r.getClient().getFirstName() + " " + r.getClient().getLastName(),
                        r.getVehicle().getLicensePlate(),
                        r.getVehicle().getVehicleModel().getName(),
                        r.getEndDate(),
                        r.getEndDate().isBefore(LocalDate.now())
                )));

        return new DashboardStatsResponse(
                totalVehicles, available, rented, maintenance,
                totalClients, activeRentals, overdueRentals,
                totalInvoices, unpaidInvoices, totalRevenue,
                Math.round(occupancyRate * 100.0) / 100.0,
                upcomingReturns
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueDataPoint> getRevenue(LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.getTenantId();

        // Group by month between from and to
        List<RevenueDataPoint> dataPoints = new ArrayList<>();
        LocalDate current = from.withDayOfMonth(1);

        while (!current.isAfter(to)) {
            LocalDate monthEnd = current.plusMonths(1).minusDays(1);
            if (monthEnd.isAfter(to)) monthEnd = to;

            // Query paid invoices in this month range
            final LocalDate start = current;
            final LocalDate end = monthEnd;

            // Use a simpler approach: get all paid invoices and filter in memory
            // For production, this should be a proper @Query with date range
            var invoices = invoiceRepository.findByTenantIdAndStatus(tenantId, InvoiceStatus.PAID, PageRequest.of(0, 1000));
            double monthRevenue = invoices.getContent().stream()
                    .filter(inv -> inv.getPaidDate() != null
                            && !inv.getPaidDate().isBefore(start)
                            && !inv.getPaidDate().isAfter(end))
                    .mapToDouble(inv -> inv.getTotalAmount())
                    .sum();
            long monthCount = invoices.getContent().stream()
                    .filter(inv -> inv.getPaidDate() != null
                            && !inv.getPaidDate().isBefore(start)
                            && !inv.getPaidDate().isAfter(end))
                    .count();

            dataPoints.add(new RevenueDataPoint(
                    current.getMonth().name() + " " + current.getYear(),
                    monthRevenue,
                    monthCount
            ));

            current = current.plusMonths(1);
        }

        return dataPoints;
    }
}
