package com.smartemi.dto;

import java.util.List;

public record CompareResponse(List<EmiSummary> loans, int lowestEmiIndex, int lowestInterestIndex, String tenureInsight) { }
