package com.loadtrack.dto;

import com.loadtrack.entity.Dealer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DealerSummaryResponse {
    private Long id;
    private String name;
    private String phone;

    public static DealerSummaryResponse from(Dealer d) {
        if (d == null) return null;
        return new DealerSummaryResponse(d.getId(), d.getName(), d.getPhone());
    }
}
