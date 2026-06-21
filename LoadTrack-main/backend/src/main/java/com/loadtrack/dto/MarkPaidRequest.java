package com.loadtrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarkPaidRequest {

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.01", message = "Paid amount must be greater than zero")
    private BigDecimal paidAmount;
}
