package com.loadtrack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApproveTripRequestRequest {

    @NotNull(message = "Truck is required")
    private Long truckId;

    @NotNull(message = "Driver is required")
    private Long driverId;

    /** Optional — if not provided, uses the dealer's requested date. */
    private LocalDate tripDate;

    @Size(max = 2000)
    private String adminNotes;
}
