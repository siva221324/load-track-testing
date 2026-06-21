package com.loadtrack.dto;

import com.loadtrack.entity.TripRequest;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TripRequestResponse {
    private Long id;
    private DealerSummaryResponse dealer;
    private SandTypeSummaryResponse sandType;
    private BigDecimal tons;
    private String sourceLocation;
    private String destinationLocation;
    private LocalDate requestedDate;
    private String notes;
    private String status;
    private String adminNotes;
    private Long approvedTripId;
    private BigDecimal estimatedAmount;   // tons × current sand-type rate
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TripRequestResponse from(TripRequest r) {
        BigDecimal estimated = r.getTons().multiply(r.getSandType().getPricePerTon());
        return TripRequestResponse.builder()
                .id(r.getId())
                .dealer(DealerSummaryResponse.from(r.getDealer()))
                .sandType(SandTypeSummaryResponse.from(r.getSandType()))
                .tons(r.getTons())
                .sourceLocation(r.getSourceLocation())
                .destinationLocation(r.getDestinationLocation())
                .requestedDate(r.getRequestedDate())
                .notes(r.getNotes())
                .status(r.getStatus())
                .adminNotes(r.getAdminNotes())
                .approvedTripId(r.getApprovedTrip() != null ? r.getApprovedTrip().getId() : null)
                .estimatedAmount(estimated)
                .respondedAt(r.getRespondedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
