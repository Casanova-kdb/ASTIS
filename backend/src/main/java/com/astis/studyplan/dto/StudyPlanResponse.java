package com.astis.studyplan.dto;

import com.astis.settings.entity.PreferredStudyTime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StudyPlanResponse(
        LocalDateTime generatedAt,
        LocalDate startDate,
        LocalDate endDate,
        int planningDays,
        PreferredStudyTime preferredStudyTime,
        BigDecimal dailyCapacityHours,
        BigDecimal totalAvailableHours,
        BigDecimal totalScheduledHours,
        BigDecimal totalUnscheduledHours,
        boolean overloaded,
        List<StudyPlanWarningResponse> warnings,
        List<DailyStudyPlanResponse> days,
        List<UnscheduledTaskResponse> unscheduledTasks
) {
}
