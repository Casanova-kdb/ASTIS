package com.astis.studyplan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlannedSession(
        Long taskId,
        String title,
        BigDecimal durationHours,
        LocalDateTime deadline,
        double priorityScore,
        String delayRisk
) {
}
