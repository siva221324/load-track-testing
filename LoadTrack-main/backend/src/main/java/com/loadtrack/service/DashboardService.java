package com.loadtrack.service;

import com.loadtrack.dto.AdminDashboardResponse;
import com.loadtrack.dto.AdminDashboardResponse.MonthlyEarningPoint;
import com.loadtrack.entity.Payment;
import com.loadtrack.entity.Trip;
import com.loadtrack.repository.DealerRepository;
import com.loadtrack.repository.DriverRepository;
import com.loadtrack.repository.PaymentRepository;
import com.loadtrack.repository.TripRepository;
import com.loadtrack.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;
    private final DealerRepository dealerRepository;
    private final TripRepository tripRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;

    public AdminDashboardResponse adminSummary() {
        final Long orgId = currentUserService.getCurrentOrgId();

        long totalTrucks = truckRepository.countByOrganizationId(orgId);
        long availableTrucks = truckRepository.countByOrganizationIdAndStatus(orgId, "AVAILABLE");
        long onTripTrucks = truckRepository.countByOrganizationIdAndStatus(orgId, "ON_TRIP");

        long totalDrivers = driverRepository.countByOrganizationId(orgId);
        long totalDealers = dealerRepository.countByOrganizationId(orgId);

        long totalTrips = tripRepository.countByOrganizationId(orgId);
        long pendingTrips = tripRepository.count(byOrgAndStatus(orgId, "PENDING"));
        long activeTrips = tripRepository.count(byOrgAndStatus(orgId, "STARTED"));
        long completedTrips = tripRepository.count(byOrgAndStatus(orgId, "COMPLETED"));

        LocalDate today = LocalDate.now();

        Specification<Payment> orgPayments = (r, q, cb) -> cb.equal(r.get("organization").get("id"), orgId);
        Specification<Payment> notPaid = orgPayments.and(
                (r, q, cb) -> cb.notEqual(r.get("paymentStatus"), "PAID"));

        long pendingPayments = paymentRepository.count(notPaid);
        long overduePayments = paymentRepository.count(
                notPaid.and((r, q, cb) -> cb.lessThan(r.get("dueDate"), today)));

        // This-month earnings: sum of paidAmount where payment_date is this month
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate firstOfNextMonth = firstOfMonth.plusMonths(1);
        BigDecimal thisMonthEarnings = paymentRepository.sumPaidBetweenForOrg(
                orgId, firstOfMonth.atStartOfDay(), firstOfNextMonth.atStartOfDay());

        BigDecimal totalBilled = paymentRepository.sumAllBilledForOrg(orgId);
        BigDecimal totalCollected = paymentRepository.sumAllPaidForOrg(orgId);

        // Monthly earnings — last 6 months including current
        List<MonthlyEarningPoint> monthly = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = firstOfMonth.minusMonths(i);
            LocalDate monthEnd = monthStart.plusMonths(1);
            BigDecimal sum = paymentRepository.sumPaidBetweenForOrg(
                    orgId, monthStart.atStartOfDay(), monthEnd.atStartOfDay());
            monthly.add(new MonthlyEarningPoint(monthStart.format(MONTH_LABEL), sum));
        }

        return AdminDashboardResponse.builder()
                .totalTrucks(totalTrucks)
                .availableTrucks(availableTrucks)
                .onTripTrucks(onTripTrucks)
                .totalDrivers(totalDrivers)
                .totalDealers(totalDealers)
                .totalTrips(totalTrips)
                .pendingTrips(pendingTrips)
                .activeTrips(activeTrips)
                .completedTrips(completedTrips)
                .pendingPayments(pendingPayments)
                .overduePayments(overduePayments)
                .thisMonthEarnings(thisMonthEarnings)
                .totalBilled(totalBilled)
                .totalCollected(totalCollected)
                .outstandingBalance(totalBilled.subtract(totalCollected))
                .monthlyEarnings(monthly)
                .build();
    }

    private Specification<Trip> byOrgAndStatus(Long orgId, String status) {
        return (r, q, cb) -> cb.and(
                cb.equal(r.get("organization").get("id"), orgId),
                cb.equal(r.get("status"), status));
    }
}
