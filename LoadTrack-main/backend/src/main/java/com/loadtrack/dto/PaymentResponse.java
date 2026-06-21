package com.loadtrack.dto;

import com.loadtrack.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private TripSummaryResponse trip;
    private BigDecimal originalAmount;
    private BigDecimal interestAmount;     // computed (live) for unpaid; stored for PAID
    private BigDecimal finalAmount;        // computed (live) for unpaid; stored for PAID
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;         // finalAmount - paidAmount
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private LocalDate dueDate;
    private boolean overdue;
    private long daysOverdue;              // 0 if not overdue
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponse from(Payment p,
                                       BigDecimal currentInterest,
                                       BigDecimal currentFinal,
                                       boolean overdue,
                                       long daysOverdue) {
        return PaymentResponse.builder()
                .id(p.getId())
                .trip(TripSummaryResponse.from(p.getTrip()))
                .originalAmount(p.getOriginalAmount())
                .interestAmount(currentInterest)
                .finalAmount(currentFinal)
                .paidAmount(p.getPaidAmount())
                .balanceDue(currentFinal.subtract(p.getPaidAmount()))
                .paymentStatus(p.getPaymentStatus())
                .paymentDate(p.getPaymentDate())
                .dueDate(p.getDueDate())
                .overdue(overdue)
                .daysOverdue(daysOverdue)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
