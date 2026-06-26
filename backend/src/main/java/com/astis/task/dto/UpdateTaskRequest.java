package com.astis.task.dto;

import com.astis.task.entity.TaskPriority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateTaskRequest(
        @NotBlank
        @Size(max = 120)
        String title,

        String description,

        @NotBlank
        @Size(max = 50)
        String taskType,

        @NotNull
        TaskPriority priority,

        @NotNull
        @FutureOrPresent
        LocalDateTime deadline,

        @DecimalMin(value = "0.0")
        BigDecimal estimatedHours
) {
}
