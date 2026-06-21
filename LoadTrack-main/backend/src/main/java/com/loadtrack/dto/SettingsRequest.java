package com.loadtrack.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettingsRequest {

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRatePercent;

    @NotNull(message = "Allowed days is required")
    @Min(value = 1, message = "Allowed days must be at least 1")
    private Integer allowedDays;
}
