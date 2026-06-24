package com.astis.task.dto;

import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String taskType,
        TaskPriority priority,
        TaskStatus status,
        LocalDateTime deadline,
        BigDecimal estimatedHours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getTaskType(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getEstimatedHours(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCompletedAt()
        );
    }
}
