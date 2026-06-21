package com.loadtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalTrucks;
    private long availableTrucks;
    private long onTripTrucks;
    private long totalDrivers;
    private long totalDealers;
    private long totalTrips;
    private long pendingTrips;
    private long activeTrips;       // STARTED
    private long completedTrips;
    private long pendingPayments;   // not PAID
    private long overduePayments;
    private BigDecimal thisMonthEarnings;
    private BigDecimal totalBilled;
    private BigDecimal totalCollected;
    private BigDecimal outstandingBalance;
    private List<MonthlyEarningPoint> monthlyEarnings;   // last 6 months

    @Data
    @AllArgsConstructor
    public static class MonthlyEarningPoint {
        private String label;          // e.g. "Jan 2026"
        private BigDecimal amount;
    }
}
