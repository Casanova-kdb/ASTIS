package com.astis.studyplan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record StudySessionResponse(
        Long taskId,
        String title,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal durationHours,
        LocalDateTime deadline,
        double priorityScore,
        String delayRisk
) {
}
