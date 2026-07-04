package com.astis.settings.dto;

import com.astis.settings.entity.UserProfile;
import java.time.LocalDateTime;

public record UserProfileResponse(
        String studyPace,
        String deadlinePressureTolerance,
        String dailyStudyCapacity,
        String preferredStudyTime,
        String planningStyle,
        LocalDateTime updatedAt
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getStudyPace().name(),
                profile.getDeadlinePressureTolerance().name(),
                profile.getDailyStudyCapacity().name(),
                profile.getPreferredStudyTime().name(),
                profile.getPlanningStyle().name(),
                profile.getUpdatedAt()
        );
    }
}
