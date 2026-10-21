package com.smartemi.controller;

import com.smartemi.dto.DashboardData;
import com.smartemi.service.CalculationService;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class ApiDashboardController {
    private final CalculationService service;

    public ApiDashboardController(CalculationService service) { this.service = service; }

    @GetMapping
    public DashboardData dashboard(Principal principal) {
        return service.dashboard(principal.getName());
    }
}
