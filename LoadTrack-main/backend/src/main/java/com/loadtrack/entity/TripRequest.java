package com.loadtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dealer dealer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sand_type_id", nullable = false)
    private SandType sandType;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tons;

    @Column(name = "source_location", nullable = false, length = 200)
    private String sourceLocation;

    @Column(name = "destination_location", nullable = false, length = 200)
    private String destinationLocation;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_trip_id")
    private Trip approvedTrip;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
