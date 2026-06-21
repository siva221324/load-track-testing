package com.loadtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class DealerStatsResponse {
    private Long dealerId;
    private String dealerName;
    private long totalTrips;
    private long totalPayments;
    private long pendingPayments;
    private long overduePayments;
    private BigDecimal totalBilled;        // sum of finalAmount across all payments
    private BigDecimal totalPaid;          // sum of paidAmount
    private BigDecimal totalBalance;       // billed - paid
    private BigDecimal thisMonthBilled;    // sum of finalAmount for trips in this month
    private BigDecimal thisMonthPaid;      // sum of paidAmount where payment_date in this month
}
