package com.smartemi.dto;

import java.math.BigDecimal;

/** Intermediate values shown in the "How this was calculated" section. */
public record CalcExplanation(int tenureValue, boolean years, int months, BigDecimal monthlyRate,
                              BigDecimal growthFactor, ScheduleRow first, ScheduleRow last, boolean zeroInterest) { }
