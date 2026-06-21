package com.loadtrack.controller;

import com.loadtrack.dto.MarkPaidRequest;
import com.loadtrack.dto.PaymentResponse;
import com.loadtrack.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public Page<PaymentResponse> list(
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean overdueOnly,
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return paymentService.list(dealerId, status, overdueOnly, pageable);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return paymentService.get(id);
    }

    @PostMapping("/{id}/pay")
    public PaymentResponse markAsPaid(@PathVariable Long id, @Valid @RequestBody MarkPaidRequest req) {
        return paymentService.markAsPaid(id, req.getPaidAmount());
    }
}
