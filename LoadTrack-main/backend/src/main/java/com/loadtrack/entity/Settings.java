package com.loadtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(name = "interest_rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRatePercent;

    @Column(name = "allowed_days", nullable = false)
    private Integer allowedDays;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
