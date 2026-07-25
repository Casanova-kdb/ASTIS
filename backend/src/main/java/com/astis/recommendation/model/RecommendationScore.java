package com.astis.recommendation.model;

import java.util.List;

public record RecommendationScore(
        Long taskId,
        double priorityScore,
        DelayRiskLevel delayRisk,
        String reason,
        List<String> explanationFactors,
        String delayRiskReason
) {
}
