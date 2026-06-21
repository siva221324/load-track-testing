package com.loadtrack.service;

import com.loadtrack.dto.DriverStatsResponse;
import com.loadtrack.dto.TripResponse;
import com.loadtrack.entity.Driver;
import com.loadtrack.entity.Trip;
import com.loadtrack.repository.DriverRepository;
import com.loadtrack.repository.TripRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverPortalService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final CurrentUserService currentUserService;

    public Page<TripResponse> myTrips(String status, Pageable pageable) {
        Long driverId = currentUserService.getCurrentDriverId();
        Specification<Trip> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("driver").get("id"), driverId));
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return tripRepository.findAll(spec, pageable).map(TripResponse::from);
    }

    public DriverStatsResponse myStats() {
        Long driverId = currentUserService.getCurrentDriverId();
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Driver not found"));

        Specification<Trip> mine = (root, q, cb) -> cb.equal(root.get("driver").get("id"), driverId);
        long total = tripRepository.count(mine);
        long completed = tripRepository.count(mine.and((r, q, cb) -> cb.equal(r.get("status"), "COMPLETED")));
        long pending = tripRepository.count(mine.and((r, q, cb) -> cb.equal(r.get("status"), "PENDING")));
        long inProgress = tripRepository.count(mine.and((r, q, cb) -> cb.equal(r.get("status"), "STARTED")));

        BigDecimal earnings = driver.getSalaryPerTrip().multiply(BigDecimal.valueOf(completed));

        // This-month stats
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate firstOfNextMonth = firstOfMonth.plusMonths(1);

        Specification<Trip> mineThisMonth = mine.and((r, q, cb) ->
                cb.and(
                        cb.greaterThanOrEqualTo(r.get("tripDate"), firstOfMonth),
                        cb.lessThan(r.get("tripDate"), firstOfNextMonth)
                ));
        long monthTrips = tripRepository.count(mineThisMonth);
        long monthCompleted = tripRepository.count(
                mineThisMonth.and((r, q, cb) -> cb.equal(r.get("status"), "COMPLETED")));
        BigDecimal monthEarnings = driver.getSalaryPerTrip().multiply(BigDecimal.valueOf(monthCompleted));

        return DriverStatsResponse.builder()
                .driverId(driver.getId())
                .driverName(driver.getName())
                .totalTrips(total)
                .completedTrips(completed)
                .pendingTrips(pending)
                .inProgressTrips(inProgress)
                .salaryPerTrip(driver.getSalaryPerTrip())
                .totalEarnings(earnings)
                .thisMonthTrips(monthTrips)
                .thisMonthCompleted(monthCompleted)
                .thisMonthEarnings(monthEarnings)
                .build();
    }
}
