package com.smartemi.controller;

import com.smartemi.dto.*;
import com.smartemi.entity.LoanType;
import com.smartemi.service.CalculationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculations")
public class ApiCalculationController {
    private final CalculationService service;

    public ApiCalculationController(CalculationService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<CalculationResponse> save(@Valid @RequestBody EmiRequest req, Principal principal) {
        CalculationResponse saved = service.save(principal.getName(), req);
        return ResponseEntity.created(URI.create("/api/calculations/" + saved.id())).body(saved);
    }

    @GetMapping
    public PageResponse<CalculationResponse> list(Principal principal,
            @RequestParam(required = false) LoanType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return PageResponse.of(service.search(principal.getName(), type, from, to, page, Math.min(Math.max(size, 1), 100)));
    }

    @GetMapping("/{id}")
    public CalculationResponse get(@PathVariable Long id, Principal principal) {
        return service.get(principal.getName(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        service.delete(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
