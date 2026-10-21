package com.smartemi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduleRow(int installment, LocalDate date, BigDecimal openingBalance, BigDecimal emi,
                          BigDecimal principalPaid, BigDecimal interestPaid, BigDecimal closingBalance) { }
