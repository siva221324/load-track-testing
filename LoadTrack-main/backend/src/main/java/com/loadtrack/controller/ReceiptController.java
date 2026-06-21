package com.loadtrack.controller;

import com.loadtrack.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/payment/{paymentId}/download")
    public ResponseEntity<byte[]> downloadByPayment(@PathVariable Long paymentId) {
        byte[] pdf = receiptService.generatePdf(paymentId);
        String filename = receiptService.getReceiptNumber(paymentId) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .body(pdf);
    }
}
