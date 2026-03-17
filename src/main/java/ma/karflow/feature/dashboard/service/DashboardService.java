package ma.karflow.feature.dashboard.service;

import ma.karflow.feature.dashboard.dto.DashboardStatsResponse;
import ma.karflow.feature.dashboard.dto.RevenueDataPoint;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardStatsResponse getStats();
    List<RevenueDataPoint> getRevenue(LocalDate from, LocalDate to);
}
