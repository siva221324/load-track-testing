package com.loadtrack.dto;

import com.loadtrack.entity.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DriverSummaryResponse {
    private Long id;
    private String name;
    private String phone;

    public static DriverSummaryResponse from(Driver d) {
        if (d == null) return null;
        return new DriverSummaryResponse(d.getId(), d.getName(), d.getPhone());
    }
}
