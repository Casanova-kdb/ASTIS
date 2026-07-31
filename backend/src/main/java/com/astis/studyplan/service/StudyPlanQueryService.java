package com.astis.studyplan.service;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.recommendation.service.RecommendationRankingService;
import com.astis.settings.entity.UserProfile;
import com.astis.settings.repository.UserProfileRepository;
import com.astis.studyplan.dto.StudyPlanResponse;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudyPlanQueryService {

    private final AppUserRepository appUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final RecommendationRankingService recommendationRankingService;
    private final StudyPlanGeneratorService studyPlanGeneratorService;

    public StudyPlanQueryService(
            AppUserRepository appUserRepository,
            UserProfileRepository userProfileRepository,
            RecommendationRankingService recommendationRankingService,
            StudyPlanGeneratorService studyPlanGeneratorService
    ) {
        this.appUserRepository = appUserRepository;
        this.userProfileRepository = userProfileRepository;
        this.recommendationRankingService = recommendationRankingService;
        this.studyPlanGeneratorService = studyPlanGeneratorService;
    }

    @Transactional
    public StudyPlanResponse generateStudyPlan(String userEmail, int planningDays) {
        validatePlanningDays(planningDays);
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> userProfileRepository.save(new UserProfile(user)));
        List<RecommendedTaskResponse> recommendations =
                recommendationRankingService.rankActiveTasks(user);

        return studyPlanGeneratorService.generate(
                recommendations,
                profile.getDailyStudyCapacity(),
                profile.getPreferredStudyTime(),
                LocalDateTime.now(),
                planningDays
        );
    }

    private void validatePlanningDays(int planningDays) {
        if (planningDays < StudyPlanGeneratorService.MIN_PLANNING_DAYS
                || planningDays > StudyPlanGeneratorService.MAX_PLANNING_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Planning days must be between 1 and 14"
            );
        }
    }
}
