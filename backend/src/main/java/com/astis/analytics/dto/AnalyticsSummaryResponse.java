package com.astis.analytics.dto;

public record AnalyticsSummaryResponse(
        long totalTaskCount,
        long completedTaskCount,
        long overdueTaskCount,
        double completionRate
) {
}
