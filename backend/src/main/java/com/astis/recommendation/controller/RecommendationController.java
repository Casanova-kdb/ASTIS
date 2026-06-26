package com.astis.recommendation.controller;

import com.astis.ai.dto.AiStudyAdviceResponse;
import com.astis.ai.service.AiAdviceService;
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
    private final AiAdviceService aiAdviceService;

    public RecommendationController(
            RecommendationQueryService recommendationQueryService,
            AiAdviceService aiAdviceService
    ) {
        this.recommendationQueryService = recommendationQueryService;
        this.aiAdviceService = aiAdviceService;
    }

    @GetMapping("/tasks")
    public ApiResponse<List<RecommendedTaskResponse>> getRecommendedTasks(Principal principal) {
        return ApiResponse.success(
                "Recommended tasks retrieved",
                recommendationQueryService.getRecommendedTasks(principal.getName())
        );
    }

    @GetMapping("/advice")
    public ApiResponse<AiStudyAdviceResponse> getStudyAdvice(Principal principal) {
        return ApiResponse.success("AI study advice generated", aiAdviceService.generateStudyAdvice(principal.getName()));
    }
}
