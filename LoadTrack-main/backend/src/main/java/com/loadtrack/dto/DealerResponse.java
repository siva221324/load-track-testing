package com.loadtrack.dto;

import com.loadtrack.entity.Dealer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DealerResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private LoginInfoResponse loginInfo;  // populated only on create — auto-generated login
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DealerResponse from(Dealer d) {
        return DealerResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .phone(d.getPhone())
                .address(d.getAddress())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
