package com.astis.recommendation.dto;

import com.astis.recommendation.model.RecommendationScore;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecommendedTaskResponse(
        int rankPosition,
        Long taskId,
        String title,
        String taskType,
        TaskPriority priority,
        TaskStatus status,
        LocalDateTime deadline,
        BigDecimal estimatedHours,
        double priorityScore,
        String delayRisk,
        String reason
) {
    public static RecommendedTaskResponse from(int rankPosition, Task task, RecommendationScore score) {
        return new RecommendedTaskResponse(
                rankPosition,
                task.getId(),
                task.getTitle(),
                task.getTaskType(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getEstimatedHours(),
                score.priorityScore(),
                score.delayRisk().name(),
                score.reason()
        );
    }
}
