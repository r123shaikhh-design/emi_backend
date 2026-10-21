package com.smartemi.controller;

import com.smartemi.dto.EmiRequest;
import com.smartemi.dto.EmiResult;
import com.smartemi.service.EmiService;
import com.smartemi.service.ExportService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Downloads driven by query parameters, e.g. /api/emi/export/pdf?loanAmount=100000&annualRate=12&tenure=12&tenureUnit=MONTHS&loanType=CAR */
@RestController
@RequestMapping("/api/emi/export")
public class ApiExportController {
    private final EmiService emiService;
    private final ExportService exportService;

    public ApiExportController(EmiService emiService, ExportService exportService) {
        this.emiService = emiService; this.exportService = exportService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@Valid @ModelAttribute EmiRequest req, BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().build();
        EmiResult r = emiService.calculate(req);
        return download(exportService.summaryPdf(r.summary(), req.getLoanType()), "emi-summary.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> csv(@Valid @ModelAttribute EmiRequest req, BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().build();
        return download(exportService.scheduleCsv(emiService.calculate(req).schedule()),
                "amortization-schedule.csv", MediaType.parseMediaType("text/csv"));
    }

    private ResponseEntity<byte[]> download(byte[] body, String name, MediaType type) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(name).build().toString())
                .contentType(type).body(body);
    }
}
