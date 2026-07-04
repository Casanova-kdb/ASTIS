package com.astis.recommendation.model;

public record RecommendationFeatures(
        Long taskId,
        Long userId,
        long daysUntilDeadline,
        double estimatedHours,
        double urgencyScore,
        double completionRateScore,
        double userPriorityScore,
        double timeDecayScore,
        double delayRiskScore,
        double workloadScore,
        double gradeWeightScore,
        double difficultyScore,
        double deadlineFlexibilityScore,
        double personalImportanceScore,
        double overdueTaskRatio
) {
}
