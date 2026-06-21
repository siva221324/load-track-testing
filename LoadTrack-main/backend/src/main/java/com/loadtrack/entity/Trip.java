package com.loadtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(optional = false)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sand_type_id", nullable = false)
    private SandType sandType;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tons;

    @Column(name = "source_location", nullable = false, length = 200)
    private String sourceLocation;

    @Column(name = "destination_location", nullable = false, length = 200)
    private String destinationLocation;

    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;

    @Column(name = "rate_per_ton", nullable = false, precision = 12, scale = 2)
    private BigDecimal ratePerTon;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
