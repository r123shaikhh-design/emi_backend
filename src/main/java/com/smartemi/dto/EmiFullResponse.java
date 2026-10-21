package com.smartemi.dto;

import java.util.List;

public record EmiFullResponse(EmiSummary summary, CalcExplanation explanation, List<ScheduleRow> schedule) { }
