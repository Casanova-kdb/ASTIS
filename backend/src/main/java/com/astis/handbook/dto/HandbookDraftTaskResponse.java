package com.astis.handbook.dto;

import com.astis.task.entity.TaskPriority;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HandbookDraftTaskResponse(
        String title,
        String description,
        String taskType,
        TaskPriority priority,
        LocalDateTime deadline,
        boolean deadlineMissing,
        BigDecimal estimatedHours,
        Integer gradeWeight,
        Integer difficultyLevel,
        Integer deadlineFlexibility,
        Integer personalImportance,
        Integer confidenceScore,
        String sourceEvidence
) {
}
