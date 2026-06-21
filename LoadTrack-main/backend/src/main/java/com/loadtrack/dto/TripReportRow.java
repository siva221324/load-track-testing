package com.loadtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class TripReportRow {
    private Long tripId;
    private LocalDate tripDate;
    private String truckNumber;
    private String driverName;
    private String dealerName;
    private String sandTypeName;
    private BigDecimal tons;
    private BigDecimal ratePerTon;
    private BigDecimal totalAmount;
    private String status;
    private String sourceLocation;
    private String destinationLocation;
}
