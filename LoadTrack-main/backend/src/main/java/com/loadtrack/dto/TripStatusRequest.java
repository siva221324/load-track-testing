package com.loadtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TripStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|STARTED|COMPLETED",
             message = "Status must be PENDING, STARTED, or COMPLETED")
    private String status;
}
