package com.loadtrack.dto;

import com.loadtrack.entity.SandType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class SandTypeSummaryResponse {
    private Long id;
    private String name;
    private BigDecimal pricePerTon;

    public static SandTypeSummaryResponse from(SandType s) {
        if (s == null) return null;
        return new SandTypeSummaryResponse(s.getId(), s.getName(), s.getPricePerTon());
    }
}
