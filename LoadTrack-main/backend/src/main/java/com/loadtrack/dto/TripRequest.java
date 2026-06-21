package com.loadtrack.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TripRequest {

    @NotNull(message = "Truck is required")
    private Long truckId;

    @NotNull(message = "Driver is required")
    private Long driverId;

    @NotNull(message = "Dealer is required")
    private Long dealerId;

    @NotNull(message = "Sand type is required")
    private Long sandTypeId;

    @NotNull(message = "Tons is required")
    @DecimalMin(value = "0.01", message = "Tons must be greater than zero")
    private BigDecimal tons;

    @NotBlank(message = "Source location is required")
    @Size(max = 200)
    private String sourceLocation;

    @NotBlank(message = "Destination location is required")
    @Size(max = 200)
    private String destinationLocation;

    @NotNull(message = "Trip date is required")
    private LocalDate tripDate;
}
