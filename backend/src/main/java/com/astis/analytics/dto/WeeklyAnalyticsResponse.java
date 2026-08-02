package com.astis.analytics.dto;

import java.time.LocalDate;

public record WeeklyAnalyticsResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        long completedCount,
        long overdueCount
) {
}
