package com.astis.recommendation.model;

public record RecommendationScore(
        Long taskId,
        double priorityScore,
        DelayRiskLevel delayRisk,
        String reason
) {
}
