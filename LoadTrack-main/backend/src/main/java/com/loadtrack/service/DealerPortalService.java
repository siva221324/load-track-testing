package com.loadtrack.service;

import com.loadtrack.dto.DealerStatsResponse;
import com.loadtrack.dto.PaymentResponse;
import com.loadtrack.entity.Dealer;
import com.loadtrack.entity.Payment;
import com.loadtrack.entity.Settings;
import com.loadtrack.entity.Trip;
import com.loadtrack.repository.DealerRepository;
import com.loadtrack.repository.PaymentRepository;
import com.loadtrack.repository.SettingsRepository;
import com.loadtrack.repository.TripRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealerPortalService {

    private final PaymentRepository paymentRepository;
    private final TripRepository tripRepository;
    private final DealerRepository dealerRepository;
    private final SettingsRepository settingsRepository;
    private final CurrentUserService currentUserService;

    public Page<PaymentResponse> myPayments(String status, Boolean overdueOnly, Pageable pageable) {
        Long dealerId = currentUserService.getCurrentDealerId();
        Settings settings = loadSettings();
        LocalDate today = LocalDate.now();

        Specification<Payment> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("trip").get("dealer").get("id"), dealerId));
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("paymentStatus"), status));
            }
            if (Boolean.TRUE.equals(overdueOnly)) {
                predicates.add(cb.notEqual(root.get("paymentStatus"), "PAID"));
                predicates.add(cb.lessThan(root.get("dueDate"), today));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return paymentRepository.findAll(spec, pageable)
                .map(p -> buildResponse(p, settings, today));
    }

    public DealerStatsResponse myStats() {
        Long dealerId = currentUserService.getCurrentDealerId();
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer not found"));

        Settings settings = loadSettings();
        LocalDate today = LocalDate.now();

        Specification<Trip> myTrips = (r, q, cb) -> cb.equal(r.get("dealer").get("id"), dealerId);
        long totalTrips = tripRepository.count(myTrips);

        Specification<Payment> myPayments = (r, q, cb) -> cb.equal(r.get("trip").get("dealer").get("id"), dealerId);
        long totalPayments = paymentRepository.count(myPayments);
        long pending = paymentRepository.count(myPayments.and((r, q, cb) -> cb.notEqual(r.get("paymentStatus"), "PAID")));
        long overdue = paymentRepository.count(myPayments
                .and((r, q, cb) -> cb.notEqual(r.get("paymentStatus"), "PAID"))
                .and((r, q, cb) -> cb.lessThan(r.get("dueDate"), today)));

        // Sums computed by iterating (small data set; for larger datasets use a JPA aggregate query)
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate firstOfNextMonth = firstOfMonth.plusMonths(1);

        List<Payment> all = paymentRepository.findAll(myPayments);
        BigDecimal totalBilled = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal monthBilled = BigDecimal.ZERO;
        BigDecimal monthPaid = BigDecimal.ZERO;
        for (Payment p : all) {
            BigDecimal currentFinal;
            if ("PAID".equals(p.getPaymentStatus())) {
                currentFinal = p.getFinalAmount();
            } else {
                BigDecimal interest = p.getDueDate().isBefore(today)
                        ? p.getOriginalAmount()
                            .multiply(settings.getInterestRatePercent()
                                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                currentFinal = p.getOriginalAmount().add(interest);
            }
            totalBilled = totalBilled.add(currentFinal);
            totalPaid = totalPaid.add(p.getPaidAmount());

            LocalDate tripDate = p.getTrip().getTripDate();
            if (!tripDate.isBefore(firstOfMonth) && tripDate.isBefore(firstOfNextMonth)) {
                monthBilled = monthBilled.add(currentFinal);
            }
            if (p.getPaymentDate() != null) {
                LocalDate paid = p.getPaymentDate().toLocalDate();
                if (!paid.isBefore(firstOfMonth) && paid.isBefore(firstOfNextMonth)) {
                    monthPaid = monthPaid.add(p.getPaidAmount());
                }
            }
        }

        return DealerStatsResponse.builder()
                .dealerId(dealer.getId())
                .dealerName(dealer.getName())
                .totalTrips(totalTrips)
                .totalPayments(totalPayments)
                .pendingPayments(pending)
                .overduePayments(overdue)
                .totalBilled(totalBilled)
                .totalPaid(totalPaid)
                .totalBalance(totalBilled.subtract(totalPaid))
                .thisMonthBilled(monthBilled)
                .thisMonthPaid(monthPaid)
                .build();
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
            currentInterest = !overdue ? BigDecimal.ZERO :
                    p.getOriginalAmount()
                            .multiply(settings.getInterestRatePercent()
                                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP);
            currentFinal = p.getOriginalAmount().add(currentInterest);
            if (overdue) {
                daysOverdue = ChronoUnit.DAYS.between(p.getDueDate(), today);
            }
        }
        return PaymentResponse.from(p, currentInterest, currentFinal, overdue, daysOverdue);
    }

    private Settings loadSettings() {
        Long orgId = currentUserService.getCurrentOrgId();
        return settingsRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new IllegalStateException("Settings row missing for organization"));
    }
}
