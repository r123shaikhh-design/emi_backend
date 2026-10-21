package com.smartemi.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardData(long count, BigDecimal totalPrincipal, BigDecimal totalInterest,
                            List<CalculationResponse> recent) { }
