package com.astis.recommendation.controller;

import com.astis.common.api.ApiResponse;
import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.recommendation.service.RecommendationQueryService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationQueryService recommendationQueryService;

    public RecommendationController(RecommendationQueryService recommendationQueryService) {
        this.recommendationQueryService = recommendationQueryService;
    }

    @GetMapping("/tasks")
    public ApiResponse<List<RecommendedTaskResponse>> getRecommendedTasks(Principal principal) {
        return ApiResponse.success(
                "Recommended tasks retrieved",
                recommendationQueryService.getRecommendedTasks(principal.getName())
        );
    }
}
