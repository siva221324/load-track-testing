package com.loadtrack.dto;

import com.loadtrack.entity.Trip;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TripResponse {
    private Long id;
    private TruckSummaryResponse truck;
    private DriverSummaryResponse driver;
    private DealerSummaryResponse dealer;
    private SandTypeSummaryResponse sandType;
    private BigDecimal tons;
    private String sourceLocation;
    private String destinationLocation;
    private LocalDate tripDate;
    private BigDecimal ratePerTon;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TripResponse from(Trip t) {
        return TripResponse.builder()
                .id(t.getId())
                .truck(TruckSummaryResponse.from(t.getTruck()))
                .driver(DriverSummaryResponse.from(t.getDriver()))
                .dealer(DealerSummaryResponse.from(t.getDealer()))
                .sandType(SandTypeSummaryResponse.from(t.getSandType()))
                .tons(t.getTons())
                .sourceLocation(t.getSourceLocation())
                .destinationLocation(t.getDestinationLocation())
                .tripDate(t.getTripDate())
                .ratePerTon(t.getRatePerTon())
                .totalAmount(t.getTotalAmount())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
