package com.smartemi.dto;

import java.util.List;

public record EmiResult(EmiSummary summary, List<ScheduleRow> schedule) { }
