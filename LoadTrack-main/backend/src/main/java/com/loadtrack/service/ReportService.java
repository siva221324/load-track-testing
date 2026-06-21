package com.loadtrack.service;

import com.loadtrack.dto.PaymentReportRow;
import com.loadtrack.dto.TripReportRow;
import com.loadtrack.entity.Payment;
import com.loadtrack.entity.Settings;
import com.loadtrack.entity.Trip;
import com.loadtrack.repository.PaymentRepository;
import com.loadtrack.repository.SettingsRepository;
import com.loadtrack.repository.TripRepository;
import com.loadtrack.util.ExcelUtil;
import com.loadtrack.util.ReportPdfBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final TripRepository tripRepository;
    private final PaymentRepository paymentRepository;
    private final SettingsRepository settingsRepository;
    private final ReportPdfBuilder pdfBuilder;
    private final CurrentUserService currentUserService;

    // ============================================================
    // TRIPS REPORT
    // ============================================================
    public List<TripReportRow> tripsReport(LocalDate from, LocalDate to,
                                           Long truckId, Long driverId, Long dealerId,
                                           String status) {
        Long orgId = currentUserService.getCurrentOrgId();
        Specification<Trip> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));
            if (from != null)     predicates.add(cb.greaterThanOrEqualTo(root.get("tripDate"), from));
            if (to != null)       predicates.add(cb.lessThanOrEqualTo(root.get("tripDate"), to));
            if (truckId != null)  predicates.add(cb.equal(root.get("truck").get("id"), truckId));
            if (driverId != null) predicates.add(cb.equal(root.get("driver").get("id"), driverId));
            if (dealerId != null) predicates.add(cb.equal(root.get("dealer").get("id"), dealerId));
            if (status != null && !status.isBlank())
                predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return tripRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "tripDate", "id"))
                .stream().map(this::toTripRow).toList();
    }

    private TripReportRow toTripRow(Trip t) {
        return TripReportRow.builder()
                .tripId(t.getId())
                .tripDate(t.getTripDate())
                .truckNumber(t.getTruck().getTruckNumber())
                .driverName(t.getDriver().getName())
                .dealerName(t.getDealer().getName())
                .sandTypeName(t.getSandType().getName())
                .tons(t.getTons())
                .ratePerTon(t.getRatePerTon())
                .totalAmount(t.getTotalAmount())
                .status(t.getStatus())
                .sourceLocation(t.getSourceLocation())
                .destinationLocation(t.getDestinationLocation())
                .build();
    }

    // ============================================================
    // PAYMENTS REPORT
    // ============================================================
    public List<PaymentReportRow> paymentsReport(LocalDate from, LocalDate to,
                                                  Long dealerId, String paymentStatus,
                                                  Boolean overdueOnly) {
        Long orgId = currentUserService.getCurrentOrgId();
        Settings settings = settingsRepository.findByOrganizationId(orgId).orElse(null);
        LocalDate today = LocalDate.now();

        Specification<Payment> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));
            if (from != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("trip").get("tripDate"), from));
            if (to != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("trip").get("tripDate"), to));
            if (dealerId != null)
                predicates.add(cb.equal(root.get("trip").get("dealer").get("id"), dealerId));
            if (paymentStatus != null && !paymentStatus.isBlank())
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            if (Boolean.TRUE.equals(overdueOnly)) {
                predicates.add(cb.notEqual(root.get("paymentStatus"), "PAID"));
                predicates.add(cb.lessThan(root.get("dueDate"), today));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return paymentRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(p -> toPaymentRow(p, settings, today)).toList();
    }

    private PaymentReportRow toPaymentRow(Payment p, Settings settings, LocalDate today) {
        BigDecimal currentInterest;
        BigDecimal currentFinal;
        boolean overdue;
        if ("PAID".equals(p.getPaymentStatus())) {
            currentInterest = p.getInterestAmount();
            currentFinal = p.getFinalAmount();
            overdue = false;
        } else {
            overdue = p.getDueDate().isBefore(today);
            BigDecimal rate = settings != null ? settings.getInterestRatePercent() : BigDecimal.ZERO;
            currentInterest = overdue && rate.signum() > 0
                    ? p.getOriginalAmount()
                            .multiply(rate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            currentFinal = p.getOriginalAmount().add(currentInterest);
        }
        return PaymentReportRow.builder()
                .paymentId(p.getId())
                .tripId(p.getTrip().getId())
                .tripDate(p.getTrip().getTripDate())
                .truckNumber(p.getTrip().getTruck().getTruckNumber())
                .dealerName(p.getTrip().getDealer().getName())
                .originalAmount(p.getOriginalAmount())
                .interestAmount(currentInterest)
                .finalAmount(currentFinal)
                .paidAmount(p.getPaidAmount())
                .balanceDue(currentFinal.subtract(p.getPaidAmount()))
                .paymentStatus(p.getPaymentStatus())
                .dueDate(p.getDueDate())
                .overdue(overdue)
                .build();
    }

    // ============================================================
    // EXPORTS
    // ============================================================
    public byte[] exportTripsExcel(List<TripReportRow> rows) {
        List<String> headers = List.of("ID", "Date", "Truck", "Driver", "Dealer", "Sand Type",
                "Tons", "Rate/Ton", "Total", "Status", "Source", "Destination");
        List<List<Object>> data = new ArrayList<>();
        for (TripReportRow r : rows) {
            data.add(List.of(r.getTripId(), r.getTripDate().toString(),
                    r.getTruckNumber(), r.getDriverName(), r.getDealerName(), r.getSandTypeName(),
                    r.getTons(), r.getRatePerTon(), r.getTotalAmount(),
                    r.getStatus(), r.getSourceLocation(), r.getDestinationLocation()));
        }
        return ExcelUtil.generate("Trips", headers, data);
    }

    public byte[] exportTripsPdf(List<TripReportRow> rows, String subtitle) {
        List<String> headers = List.of("ID", "Date", "Truck", "Driver", "Dealer", "Sand Type",
                "Tons", "Rate/Ton", "Total", "Status");
        List<List<Object>> data = new ArrayList<>();
        for (TripReportRow r : rows) {
            data.add(List.of(r.getTripId(), r.getTripDate().toString(),
                    r.getTruckNumber(), r.getDriverName(), r.getDealerName(), r.getSandTypeName(),
                    r.getTons(), r.getRatePerTon(), r.getTotalAmount(), r.getStatus()));
        }
        return pdfBuilder.build("Trips Report", subtitle, headers, data);
    }

    public byte[] exportPaymentsExcel(List<PaymentReportRow> rows) {
        List<String> headers = List.of("Payment ID", "Trip ID", "Trip Date", "Truck", "Dealer",
                "Original", "Interest", "Final", "Paid", "Balance", "Status", "Due Date", "Overdue");
        List<List<Object>> data = new ArrayList<>();
        for (PaymentReportRow r : rows) {
            data.add(List.of(r.getPaymentId(), r.getTripId(), r.getTripDate().toString(),
                    r.getTruckNumber(), r.getDealerName(),
                    r.getOriginalAmount(), r.getInterestAmount(), r.getFinalAmount(),
                    r.getPaidAmount(), r.getBalanceDue(),
                    r.getPaymentStatus(), r.getDueDate().toString(), r.isOverdue() ? "Yes" : "No"));
        }
        return ExcelUtil.generate("Payments", headers, data);
    }

    public byte[] exportPaymentsPdf(List<PaymentReportRow> rows, String subtitle) {
        List<String> headers = List.of("ID", "Trip", "Date", "Dealer", "Original", "Interest",
                "Final", "Paid", "Balance", "Status", "Due", "Overdue");
        List<List<Object>> data = new ArrayList<>();
        for (PaymentReportRow r : rows) {
            data.add(List.of(r.getPaymentId(), r.getTripId(), r.getTripDate().toString(),
                    r.getDealerName(),
                    r.getOriginalAmount(), r.getInterestAmount(), r.getFinalAmount(),
                    r.getPaidAmount(), r.getBalanceDue(),
                    r.getPaymentStatus(), r.getDueDate().toString(), r.isOverdue() ? "Yes" : "No"));
        }
        return pdfBuilder.build("Payments Report", subtitle, headers, data);
    }
}
