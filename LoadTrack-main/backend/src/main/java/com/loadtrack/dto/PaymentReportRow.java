package com.loadtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class PaymentReportRow {
    private Long paymentId;
    private Long tripId;
    private LocalDate tripDate;
    private String truckNumber;
    private String dealerName;
    private BigDecimal originalAmount;
    private BigDecimal interestAmount;
    private BigDecimal finalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private String paymentStatus;
    private LocalDate dueDate;
    private boolean overdue;
}
