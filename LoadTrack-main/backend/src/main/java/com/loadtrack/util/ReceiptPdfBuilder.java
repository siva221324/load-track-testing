package com.loadtrack.util;

import com.loadtrack.entity.Payment;
import com.loadtrack.entity.PaymentTransaction;
import com.loadtrack.entity.Receipt;
import com.loadtrack.entity.Trip;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReceiptPdfBuilder {

    private static final Font H1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(25, 118, 210));
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA, 14, Color.DARK_GRAY);
    private static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(25, 118, 210));
    private static final Font LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
    private static final Font VALUE = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(25, 118, 210));
    private static final Font FOOTER = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] build(Payment payment, Receipt receipt, List<PaymentTransaction> transactions) {
        Trip trip = payment.getTrip();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        addHeader(doc);
        addReceiptMeta(doc, receipt);
        addSpacing(doc);
        addDealerSection(doc, trip);
        addTripSection(doc, trip);
        addPaymentBreakdown(doc, payment);
        if (transactions != null && !transactions.isEmpty()) {
            addInstallmentsSection(doc, transactions);
        }
        addFooter(doc);

        doc.close();
        return baos.toByteArray();
    }

    private void addInstallmentsSection(Document doc, List<PaymentTransaction> transactions) {
        addSectionHeading(doc, "Payment History");

        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{1f, 2f, 2f}); } catch (Exception ignored) {}

        // Header row
        addHeaderCell(t, "#");
        addHeaderCell(t, "Date & Time");
        addHeaderCell(t, "Amount");

        BigDecimal sum = BigDecimal.ZERO;
        int idx = 1;
        for (PaymentTransaction tx : transactions) {
            addCell(t, String.valueOf(idx++), Element.ALIGN_LEFT, false);
            addCell(t, DT_FMT.format(tx.getPaidAt()), Element.ALIGN_LEFT, false);
            addCell(t, money(tx.getAmount()), Element.ALIGN_RIGHT, false);
            sum = sum.add(tx.getAmount());
        }

        // Total row
        addCell(t, "", Element.ALIGN_LEFT, true);
        addCell(t, "Total Paid", Element.ALIGN_LEFT, true);
        addCell(t, money(sum), Element.ALIGN_RIGHT, true);

        doc.add(t);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, LABEL));
        cell.setBorder(0);
        cell.setBorderWidthBottom(1f);
        cell.setBorderColorBottom(new Color(200, 200, 200));
        cell.setBackgroundColor(new Color(245, 247, 250));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, int align, boolean isTotal) {
        Font font = isTotal ? TOTAL : VALUE;
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(0);
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        if (isTotal) {
            cell.setBorderWidthTop(1f);
            cell.setBorderColorTop(new Color(25, 118, 210));
        }
        table.addCell(cell);
    }

    private void addHeader(Document doc) {
        Paragraph title = new Paragraph("LoadTrack", H1);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph("Payment Receipt", H2);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(8);
        doc.add(sub);
    }

    private void addReceiptMeta(Document doc, Receipt receipt) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(8);
        try { table.setWidths(new float[]{1f, 1f}); } catch (Exception ignored) {}

        PdfPCell left = new PdfPCell(new Phrase("Receipt #: " + receipt.getReceiptNumber(), LABEL));
        left.setBorder(0);
        left.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell right = new PdfPCell(new Phrase("Generated: " + DT_FMT.format(receipt.getGeneratedAt()), LABEL));
        right.setBorder(0);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(left);
        table.addCell(right);
        doc.add(table);
    }

    private void addDealerSection(Document doc, Trip trip) {
        addSectionHeading(doc, "Dealer Information");
        PdfPTable t = twoColTable();
        addRow(t, "Name", trip.getDealer().getName());
        addRow(t, "Phone", trip.getDealer().getPhone());
        addRow(t, "Address", trip.getDealer().getAddress() != null ? trip.getDealer().getAddress() : "—");
        doc.add(t);
    }

    private void addTripSection(Document doc, Trip trip) {
        addSectionHeading(doc, "Trip Information");
        PdfPTable t = twoColTable();
        addRow(t, "Trip Date", trip.getTripDate().toString());
        addRow(t, "Truck Number", trip.getTruck().getTruckNumber());
        addRow(t, "Truck Model", trip.getTruck().getModel());
        addRow(t, "Driver", trip.getDriver().getName() + " (" + trip.getDriver().getPhone() + ")");
        addRow(t, "Sand Type", trip.getSandType().getName());
        addRow(t, "Source", trip.getSourceLocation());
        addRow(t, "Destination", trip.getDestinationLocation());
        addRow(t, "Tons", trip.getTons().toPlainString());
        addRow(t, "Rate per Ton", money(trip.getRatePerTon()));
        doc.add(t);
    }

    private void addPaymentBreakdown(Document doc, Payment payment) {
        addSectionHeading(doc, "Payment Breakdown");
        PdfPTable t = twoColTable();
        addRow(t, "Original Amount", money(payment.getOriginalAmount()));
        addRow(t, "Interest", money(payment.getInterestAmount()));

        // Total row with emphasis
        PdfPCell labelCell = new PdfPCell(new Phrase("Final Amount", TOTAL));
        labelCell.setBorder(0);
        labelCell.setBackgroundColor(new Color(245, 247, 250));
        labelCell.setPadding(6);
        PdfPCell valueCell = new PdfPCell(new Phrase(money(payment.getFinalAmount()), TOTAL));
        valueCell.setBorder(0);
        valueCell.setBackgroundColor(new Color(245, 247, 250));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(6);
        t.addCell(labelCell);
        t.addCell(valueCell);

        addRow(t, "Paid Amount", money(payment.getPaidAmount()));
        addRow(t, "Balance Due", money(payment.getFinalAmount().subtract(payment.getPaidAmount())));
        addRow(t, "Status", payment.getPaymentStatus());
        addRow(t, "Payment Date",
                payment.getPaymentDate() != null ? DT_FMT.format(payment.getPaymentDate()) : "—");
        addRow(t, "Due Date", payment.getDueDate().toString());
        doc.add(t);
    }

    private void addFooter(Document doc) {
        Paragraph footer = new Paragraph(
                "Thank you for your business. Generated by LoadTrack on "
                        + DT_FMT.format(LocalDateTime.now()), FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        doc.add(footer);
    }

    private void addSectionHeading(Document doc, String text) {
        Paragraph p = new Paragraph(text, SECTION);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void addSpacing(Document doc) {
        doc.add(new Paragraph(" "));
    }

    private PdfPTable twoColTable() {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{1f, 2f}); } catch (Exception ignored) {}
        return t;
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL));
        labelCell.setBorder(0);
        labelCell.setPadding(4);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE));
        valueCell.setBorder(0);
        valueCell.setPadding(4);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String money(BigDecimal v) {
        if (v == null) return "Rs. 0.00";
        return "Rs. " + v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
