package com.loadtrack.dto;

import com.loadtrack.entity.Truck;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TruckResponse {
    private Long id;
    private String truckNumber;
    private String model;
    private BigDecimal capacityTons;
    private String insuranceNumber;
    private String rcNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TruckResponse from(Truck t) {
        return TruckResponse.builder()
                .id(t.getId())
                .truckNumber(t.getTruckNumber())
                .model(t.getModel())
                .capacityTons(t.getCapacityTons())
                .insuranceNumber(t.getInsuranceNumber())
                .rcNumber(t.getRcNumber())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
