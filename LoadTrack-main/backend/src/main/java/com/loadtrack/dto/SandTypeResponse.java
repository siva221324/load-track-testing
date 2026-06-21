package com.loadtrack.dto;

import com.loadtrack.entity.SandType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SandTypeResponse {
    private Long id;
    private String name;
    private BigDecimal pricePerTon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SandTypeResponse from(SandType s) {
        return SandTypeResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .pricePerTon(s.getPricePerTon())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
