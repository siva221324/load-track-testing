package com.loadtrack.repository;

import com.loadtrack.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    Optional<Payment> findByTripId(Long tripId);

    Optional<Payment> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM Payment p " +
           "WHERE p.organization.id = :orgId AND p.paymentDate >= :from AND p.paymentDate < :to")
    BigDecimal sumPaidBetweenForOrg(@Param("orgId") Long orgId,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.finalAmount), 0) FROM Payment p WHERE p.organization.id = :orgId")
    BigDecimal sumAllBilledForOrg(@Param("orgId") Long orgId);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM Payment p WHERE p.organization.id = :orgId")
    BigDecimal sumAllPaidForOrg(@Param("orgId") Long orgId);
}
