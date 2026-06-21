package com.loadtrack.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriverRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "License number is required")
    @Size(max = 30)
    private String licenseNumber;

    private String address;

    @NotNull(message = "Salary per trip is required")
    @DecimalMin(value = "0.00", message = "Salary cannot be negative")
    private BigDecimal salaryPerTrip;

    private Long assignedTruckId;  // nullable
}
