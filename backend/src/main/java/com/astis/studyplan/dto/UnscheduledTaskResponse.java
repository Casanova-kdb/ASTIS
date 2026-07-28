package com.astis.studyplan.dto;

import com.astis.studyplan.model.UnscheduledReason;
import java.math.BigDecimal;

public record UnscheduledTaskResponse(
        Long taskId,
        String title,
        BigDecimal remainingHours,
        UnscheduledReason reason
) {
}
