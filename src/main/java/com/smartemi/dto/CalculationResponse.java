package com.smartemi.dto;

import com.smartemi.entity.LoanCalculation;
import com.smartemi.entity.LoanType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CalculationResponse(Long id, String loanName, LoanType loanType, BigDecimal principalAmount,
                                  BigDecimal annualInterestRate, int tenureMonths, BigDecimal monthlyEmi,
                                  BigDecimal totalInterest, BigDecimal totalPayment, LocalDateTime createdAt) {
    public static CalculationResponse from(LoanCalculation c) {
        return new CalculationResponse(c.getId(), c.getLoanName(), c.getLoanType(), c.getPrincipalAmount(),
                c.getAnnualInterestRate(), c.getTenureMonths(), c.getMonthlyEmi(), c.getTotalInterest(),
                c.getTotalPayment(), c.getCreatedAt());
    }
}
