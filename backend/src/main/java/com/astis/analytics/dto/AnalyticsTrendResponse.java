package com.astis.analytics.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AnalyticsTrendResponse(
        LocalDateTime generatedAt,
        LocalDate startDate,
        LocalDate endDate,
        int weeks,
        List<WeeklyAnalyticsResponse> weeklyTrends,
        DelayedTaskTypeResponse mostDelayedTaskType,
        double averageEstimatedHours
) {
}
