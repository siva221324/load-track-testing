package com.loadtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trucks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "truck_number", nullable = false, length = 20)
    private String truckNumber;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "capacity_tons", nullable = false, precision = 8, scale = 2)
    private BigDecimal capacityTons;

    @Column(name = "insurance_number", length = 50)
    private String insuranceNumber;

    @Column(name = "rc_number", length = 50)
    private String rcNumber;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "AVAILABLE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
