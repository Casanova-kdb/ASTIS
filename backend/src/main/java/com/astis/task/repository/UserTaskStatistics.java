package com.astis.task.repository;

/**
 * User-level task counts reused while scoring one recommendation list.
 */
public record UserTaskStatistics(
        Long totalTaskCount,
        Long completedTaskCount,
        Long overdueTaskCount
) {
}
