package com.loadtrack.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SandTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 50)
    private String name;

    @NotNull(message = "Price per ton is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal pricePerTon;
}
