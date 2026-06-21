package com.loadtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectTripRequestRequest {

    @NotBlank(message = "Please provide a reason for rejection")
    @Size(max = 2000)
    private String reason;
}
