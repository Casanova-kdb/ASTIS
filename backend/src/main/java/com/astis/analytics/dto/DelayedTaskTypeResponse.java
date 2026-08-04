package com.astis.analytics.dto;

public record DelayedTaskTypeResponse(
        String taskType,
        long delayedCount
) {
}
