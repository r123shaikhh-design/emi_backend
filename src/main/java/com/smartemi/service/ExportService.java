package com.smartemi.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartemi.dto.EmiSummary;
import com.smartemi.dto.ScheduleRow;
import com.smartemi.entity.LoanType;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ExportService {
    private static final Color NAVY = new Color(11, 42, 91);

    public byte[] scheduleCsv(List<ScheduleRow> rows) {
        StringBuilder sb = new StringBuilder("Installment,Date,Opening Balance,EMI,Principal Repaid,Interest Paid,Closing Balance\n");
        for (ScheduleRow r : rows) {
            sb.append(r.installment()).append(',').append(r.date()).append(',')
              .append(r.openingBalance().toPlainString()).append(',').append(r.emi().toPlainString()).append(',')
              .append(r.principalPaid().toPlainString()).append(',').append(r.interestPaid().toPlainString()).append(',')
              .append(r.closingBalance().toPlainString()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] summaryPdf(EmiSummary s, LoanType type) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("Smart EMI Calculator - Loan Summary",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, NAVY)));
            doc.add(new Paragraph(" "));
            PdfPTable t = new PdfPTable(2);
            t.setWidthPercentage(100);
            row(t, "Loan category", type.getLabel());
            row(t, "Loan amount", money(s.loanAmount()));
            row(t, "Annual interest rate", s.annualRate().toPlainString() + "%");
            row(t, "Tenure", s.tenureMonths() + " months (" + String.format("%.1f", s.tenureMonths() / 12.0) + " years)");
            row(t, "Monthly EMI", money(s.monthlyEmi()));
            row(t, "Total interest payable", money(s.totalInterest()));
            row(t, "Total repayment", money(s.totalPayment()));
            doc.add(t);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(EmiService.DISCLAIMER, FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY)));
            doc.close();
            return out.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
    }

    private void row(PdfPTable t, String label, String value) {
        PdfPCell a = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NAVY)));
        PdfPCell b = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 11)));
        a.setPadding(8); b.setPadding(8);
        t.addCell(a); t.addCell(b);
    }

    private String money(BigDecimal v) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN"));
        nf.setMinimumFractionDigits(2); nf.setMaximumFractionDigits(2);
        return "INR " + nf.format(v);
    }
}
