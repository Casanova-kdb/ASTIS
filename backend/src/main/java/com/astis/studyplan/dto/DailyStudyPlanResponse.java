package com.astis.studyplan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyStudyPlanResponse(
        LocalDate date,
        BigDecimal totalScheduledHours,
        List<StudySessionResponse> sessions
) {
}
