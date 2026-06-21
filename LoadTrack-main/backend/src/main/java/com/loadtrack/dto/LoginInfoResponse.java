package com.loadtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginInfoResponse {
    private Long userId;
    private String username;
    private String role;
    private Long linkedToId;       // driver or dealer id
    private String linkedToName;   // their display name
}
