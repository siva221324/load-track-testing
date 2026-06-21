package com.loadtrack.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTripRequestRequest {

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

    @NotNull(message = "Requested date is required")
    @FutureOrPresent(message = "Requested date cannot be in the past")
    private LocalDate requestedDate;

    @Size(max = 2000)
    private String notes;
}
