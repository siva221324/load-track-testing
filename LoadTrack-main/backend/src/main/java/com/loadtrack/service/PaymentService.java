package com.loadtrack.service;

import com.loadtrack.dto.PaymentResponse;
import com.loadtrack.entity.Payment;
import com.loadtrack.entity.PaymentTransaction;
import com.loadtrack.entity.Settings;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.PaymentRepository;
import com.loadtrack.repository.PaymentTransactionRepository;
import com.loadtrack.repository.SettingsRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final SettingsRepository settingsRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<PaymentResponse> list(Long dealerId, String status, Boolean overdueOnly, Pageable pageable) {
        Long orgId = currentUserService.getCurrentOrgId();
        Settings settings = loadSettings(orgId);
        LocalDate today = LocalDate.now();

        Specification<Payment> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));
            if (dealerId != null)
                predicates.add(cb.equal(root.get("trip").get("dealer").get("id"), dealerId));
            if (status != null && !status.isBlank())
                predicates.add(cb.equal(root.get("paymentStatus"), status));
            if (Boolean.TRUE.equals(overdueOnly)) {
                predicates.add(cb.notEqual(root.get("paymentStatus"), "PAID"));
                predicates.add(cb.lessThan(root.get("dueDate"), today));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return paymentRepository.findAll(spec, pageable)
                .map(p -> buildResponse(p, settings, today));
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        Payment p = findOrThrow(id);
        return buildResponse(p, loadSettings(orgId), LocalDate.now());
    }

    public PaymentResponse markAsPaid(Long id, BigDecimal paidAmount) {
        Long orgId = currentUserService.getCurrentOrgId();
        Payment p = findOrThrow(id);
        if ("PAID".equals(p.getPaymentStatus())) {
            throw new IllegalStateException("Payment already fully paid");
        }

        Settings settings = loadSettings(orgId);
        LocalDate today = LocalDate.now();

        BigDecimal currentInterest = computeInterest(p, settings, today);
        BigDecimal currentFinal = p.getOriginalAmount().add(currentInterest);

        BigDecimal newPaidTotal = p.getPaidAmount().add(paidAmount);
        if (newPaidTotal.compareTo(currentFinal) > 0) {
            BigDecimal overpay = newPaidTotal.subtract(currentFinal);
            throw new IllegalArgumentException(
                    "Payment exceeds amount due by " + overpay + " (final amount: " + currentFinal + ")");
        }

        // Snapshot interest into the entity since we're persisting state change
        p.setInterestAmount(currentInterest);
        p.setFinalAmount(currentFinal);
        p.setPaidAmount(newPaidTotal);

        if (newPaidTotal.compareTo(currentFinal) == 0) {
            p.setPaymentStatus("PAID");
            p.setPaymentDate(LocalDateTime.now());
        } else {
            p.setPaymentStatus("PARTIAL");
        }

        // Record this individual installment (used by receipt PDF)
        transactionRepository.save(PaymentTransaction.builder()
                .payment(p)
                .amount(paidAmount)
                .build());

        return buildResponse(p, settings, today);
    }

    private PaymentResponse buildResponse(Payment p, Settings settings, LocalDate today) {
        BigDecimal currentInterest;
        BigDecimal currentFinal;
        boolean overdue;
        long daysOverdue = 0;

        if ("PAID".equals(p.getPaymentStatus())) {
            currentInterest = p.getInterestAmount();
            currentFinal = p.getFinalAmount();
            overdue = false;
        } else {
            overdue = p.getDueDate().isBefore(today);
            currentInterest = computeInterest(p, settings, today);
            currentFinal = p.getOriginalAmount().add(currentInterest);
            if (overdue) {
                daysOverdue = ChronoUnit.DAYS.between(p.getDueDate(), today);
            }
        }

        return PaymentResponse.from(p, currentInterest, currentFinal, overdue, daysOverdue);
    }

    private BigDecimal computeInterest(Payment p, Settings settings, LocalDate today) {
        if (!p.getDueDate().isBefore(today)) return BigDecimal.ZERO;
        BigDecimal rate = settings.getInterestRatePercent()
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return p.getOriginalAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private Payment findOrThrow(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        return paymentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    private Settings loadSettings(Long orgId) {
        return settingsRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new IllegalStateException(
                        "Settings row missing for organization — open /app/settings and save once"));
    }
}
