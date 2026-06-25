package com.astis.ai.dto;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import java.time.LocalDateTime;
import java.util.List;

public record AiStudyAdviceResponse(
        String provider,
        String model,
        boolean fallback,
        String advice,
        List<RecommendedTaskResponse> basedOnTasks,
        LocalDateTime generatedAt
) {
}
