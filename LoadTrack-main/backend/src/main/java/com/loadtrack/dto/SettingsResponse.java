package com.loadtrack.dto;

import com.loadtrack.entity.Settings;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SettingsResponse {
    private Long id;
    private BigDecimal interestRatePercent;
    private Integer allowedDays;
    private LocalDateTime updatedAt;

    public static SettingsResponse from(Settings s) {
        return SettingsResponse.builder()
                .id(s.getId())
                .interestRatePercent(s.getInterestRatePercent())
                .allowedDays(s.getAllowedDays())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
