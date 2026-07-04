package com.astis.settings.dto;

import com.astis.settings.entity.DailyStudyCapacity;
import com.astis.settings.entity.DeadlinePressureTolerance;
import com.astis.settings.entity.PlanningStyle;
import com.astis.settings.entity.PreferredStudyTime;
import com.astis.settings.entity.StudyPace;
import jakarta.validation.constraints.NotNull;

public record UpdateUserProfileRequest(
        @NotNull StudyPace studyPace,
        @NotNull DeadlinePressureTolerance deadlinePressureTolerance,
        @NotNull DailyStudyCapacity dailyStudyCapacity,
        @NotNull PreferredStudyTime preferredStudyTime,
        @NotNull PlanningStyle planningStyle
) {
}
