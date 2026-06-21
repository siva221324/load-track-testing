package com.loadtrack.dto;

import com.loadtrack.entity.Truck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TruckSummaryResponse {
    private Long id;
    private String truckNumber;
    private String model;

    public static TruckSummaryResponse from(Truck t) {
        if (t == null) return null;
        return new TruckSummaryResponse(t.getId(), t.getTruckNumber(), t.getModel());
    }
}
