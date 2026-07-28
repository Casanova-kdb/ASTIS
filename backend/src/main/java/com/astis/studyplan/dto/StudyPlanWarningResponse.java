package com.astis.studyplan.dto;

import com.astis.studyplan.model.StudyPlanWarningCode;

public record StudyPlanWarningResponse(
        StudyPlanWarningCode code,
        Long taskId,
        String message
) {
}
