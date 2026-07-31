package com.astis.recommendation.service;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecommendationQueryService {

    private final AppUserRepository appUserRepository;
    private final RecommendationRankingService recommendationRankingService;

    public RecommendationQueryService(
            AppUserRepository appUserRepository,
            RecommendationRankingService recommendationRankingService
    ) {
        this.appUserRepository = appUserRepository;
        this.recommendationRankingService = recommendationRankingService;
    }

    @Transactional(readOnly = true)
    public List<RecommendedTaskResponse> getRecommendedTasks(String userEmail) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        return recommendationRankingService.rankActiveTasks(user);
    }
}
