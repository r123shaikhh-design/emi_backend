package com.smartemi.dto;

import java.math.BigDecimal;

public record EmiSummary(BigDecimal loanAmount, BigDecimal annualRate, int tenureMonths,
                         BigDecimal monthlyEmi, BigDecimal totalInterest, BigDecimal totalPayment) { }
