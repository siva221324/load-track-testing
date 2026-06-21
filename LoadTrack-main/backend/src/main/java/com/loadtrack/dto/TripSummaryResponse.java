package com.loadtrack.dto;

import com.loadtrack.entity.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class TripSummaryResponse {
    private Long id;
    private LocalDate tripDate;
    private String truckNumber;
    private String driverName;
    private String dealerName;
    private String sandTypeName;
    private BigDecimal totalAmount;

    public static TripSummaryResponse from(Trip t) {
        if (t == null) return null;
        return TripSummaryResponse.builder()
                .id(t.getId())
                .tripDate(t.getTripDate())
                .truckNumber(t.getTruck() != null ? t.getTruck().getTruckNumber() : null)
                .driverName(t.getDriver() != null ? t.getDriver().getName() : null)
                .dealerName(t.getDealer() != null ? t.getDealer().getName() : null)
                .sandTypeName(t.getSandType() != null ? t.getSandType().getName() : null)
                .totalAmount(t.getTotalAmount())
                .build();
    }
}
