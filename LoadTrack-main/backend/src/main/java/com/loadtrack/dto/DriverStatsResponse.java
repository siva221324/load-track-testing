package com.loadtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class DriverStatsResponse {
    private Long driverId;
    private String driverName;
    private long totalTrips;
    private long completedTrips;
    private long pendingTrips;
    private long inProgressTrips;
    private BigDecimal salaryPerTrip;
    private BigDecimal totalEarnings;       // completedTrips × salaryPerTrip
    private long thisMonthTrips;            // trip_date in current month
    private long thisMonthCompleted;
    private BigDecimal thisMonthEarnings;   // thisMonthCompleted × salaryPerTrip
}
