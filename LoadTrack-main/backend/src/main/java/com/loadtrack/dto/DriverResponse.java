package com.loadtrack.dto;

import com.loadtrack.entity.Driver;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DriverResponse {
    private Long id;
    private String name;
    private String phone;
    private String licenseNumber;
    private String address;
    private BigDecimal salaryPerTrip;
    private TruckSummaryResponse assignedTruck;  // nullable
    private LoginInfoResponse loginInfo;         // populated only on create — auto-generated login
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DriverResponse from(Driver d) {
        return DriverResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .phone(d.getPhone())
                .licenseNumber(d.getLicenseNumber())
                .address(d.getAddress())
                .salaryPerTrip(d.getSalaryPerTrip())
                .assignedTruck(TruckSummaryResponse.from(d.getAssignedTruck()))
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
