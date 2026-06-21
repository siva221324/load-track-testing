package com.loadtrack.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DealerRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private String address;
}
