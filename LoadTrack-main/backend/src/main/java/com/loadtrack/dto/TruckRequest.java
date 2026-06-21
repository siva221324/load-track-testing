package com.loadtrack.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TruckRequest {

    @NotBlank(message = "Truck number is required")
    @Size(max = 20, message = "Truck number must be at most 20 characters")
    private String truckNumber;

    @NotBlank(message = "Model is required")
    @Size(max = 100)
    private String model;

    @NotNull(message = "Capacity is required")
    @DecimalMin(value = "0.01", message = "Capacity must be greater than zero")
    private BigDecimal capacityTons;

    @Size(max = 50)
    private String insuranceNumber;

    @Size(max = 50)
    private String rcNumber;

    @Pattern(regexp = "AVAILABLE|ON_TRIP|MAINTENANCE",
             message = "Status must be AVAILABLE, ON_TRIP, or MAINTENANCE")
    private String status;
}
