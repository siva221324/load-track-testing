package com.loadtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Payment payment;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "paid_at", nullable = false, updatable = false)
    private LocalDateTime paidAt;
}
