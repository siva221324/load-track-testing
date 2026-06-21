package com.loadtrack.service;

import com.loadtrack.entity.Payment;
import com.loadtrack.entity.PaymentTransaction;
import com.loadtrack.entity.Receipt;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.PaymentRepository;
import com.loadtrack.repository.PaymentTransactionRepository;
import com.loadtrack.repository.ReceiptRepository;
import com.loadtrack.util.ReceiptPdfBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiptService {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final ReceiptPdfBuilder pdfBuilder;
    private final CurrentUserService currentUserService;

    /**
     * Find existing receipt for the payment, or create one. Then render PDF in-memory.
     * Returns the PDF bytes — NEVER stored on disk (Render's filesystem is ephemeral).
     */
    public byte[] generatePdf(Long paymentId) {
        Long orgId = currentUserService.getCurrentOrgId();
        Payment payment = paymentRepository.findByIdAndOrganizationId(paymentId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        Receipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseGet(() -> receiptRepository.save(Receipt.builder()
                        .payment(payment)
                        .receiptNumber("REC-" + paymentId)
                        .build()));

        List<PaymentTransaction> transactions =
                transactionRepository.findByPaymentIdOrderByPaidAtAsc(paymentId);

        return pdfBuilder.build(payment, receipt, transactions);
    }

    public String getReceiptNumber(Long paymentId) {
        return receiptRepository.findByPaymentId(paymentId)
                .map(Receipt::getReceiptNumber)
                .orElse("REC-" + paymentId);
    }
}
