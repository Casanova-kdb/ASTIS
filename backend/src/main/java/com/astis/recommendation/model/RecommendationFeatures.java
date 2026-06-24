package com.astis.recommendation.model;

public record RecommendationFeatures(
        double urgencyScore,
        double completionRateScore,
        double userPriorityScore,
        double timeDecayScore,
        double delayRiskScore
) {
}
