package com.loadtrack.controller;

import com.loadtrack.dto.PaymentReportRow;
import com.loadtrack.dto.TripReportRow;
import com.loadtrack.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ====== Trips ======
    @GetMapping("/trips")
    public List<TripReportRow> trips(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long truckId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String status) {
        return reportService.tripsReport(from, to, truckId, driverId, dealerId, status);
    }

    @GetMapping("/trips/export")
    public ResponseEntity<byte[]> exportTrips(
            @RequestParam String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long truckId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String status) {
        List<TripReportRow> rows = reportService.tripsReport(from, to, truckId, driverId, dealerId, status);
        String subtitle = buildSubtitle(from, to, status);

        if ("xlsx".equalsIgnoreCase(format)) {
            byte[] body = reportService.exportTripsExcel(rows);
            return fileResponse(body, "trips-report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        byte[] body = reportService.exportTripsPdf(rows, subtitle);
        return fileResponse(body, "trips-report.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    // ====== Payments ======
    @GetMapping("/payments")
    public List<PaymentReportRow> payments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) Boolean overdueOnly) {
        return reportService.paymentsReport(from, to, dealerId, paymentStatus, overdueOnly);
    }

    @GetMapping("/payments/export")
    public ResponseEntity<byte[]> exportPayments(
            @RequestParam String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) Boolean overdueOnly) {
        List<PaymentReportRow> rows = reportService.paymentsReport(from, to, dealerId, paymentStatus, overdueOnly);
        String subtitle = buildSubtitle(from, to, paymentStatus);

        if ("xlsx".equalsIgnoreCase(format)) {
            byte[] body = reportService.exportPaymentsExcel(rows);
            return fileResponse(body, "payments-report.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        byte[] body = reportService.exportPaymentsPdf(rows, subtitle);
        return fileResponse(body, "payments-report.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private String buildSubtitle(LocalDate from, LocalDate to, String status) {
        StringBuilder sb = new StringBuilder();
        if (from != null || to != null) {
            sb.append("Period: ").append(from != null ? from : "(any)").append(" to ").append(to != null ? to : "(any)");
        }
        if (status != null && !status.isBlank()) {
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append("Status: ").append(status);
        }
        return sb.toString();
    }

    private ResponseEntity<byte[]> fileResponse(byte[] body, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .body(body);
    }
}
