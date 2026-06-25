package com.astis.recommendation.service;

import com.astis.recommendation.model.DelayRiskLevel;
import com.astis.recommendation.model.RecommendationFeatures;
import com.astis.recommendation.model.RecommendationScore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private static final double URGENCY_WEIGHT = 0.35;
    private static final double COMPLETION_RATE_WEIGHT = 0.15;
    private static final double USER_PRIORITY_WEIGHT = 0.25;
    private static final double TIME_DECAY_WEIGHT = 0.10;
    private static final double DELAY_RISK_WEIGHT = 0.10;
    private static final double WORKLOAD_WEIGHT = 0.05;

    public RecommendationScore scoreTask(RecommendationFeatures features) {
        double priorityScore = calculatePriorityScore(features);
        DelayRiskLevel delayRisk = classifyDelayRisk(features.delayRiskScore());
        return new RecommendationScore(
                features.taskId(),
                priorityScore,
                delayRisk,
                buildReason(features, priorityScore, delayRisk)
        );
    }

    public double calculatePriorityScore(RecommendationFeatures features) {
        double weightedScore = features.urgencyScore() * URGENCY_WEIGHT
                + features.completionRateScore() * COMPLETION_RATE_WEIGHT
                + features.userPriorityScore() * USER_PRIORITY_WEIGHT
                + features.timeDecayScore() * TIME_DECAY_WEIGHT
                + features.delayRiskScore() * DELAY_RISK_WEIGHT
                + features.workloadScore() * WORKLOAD_WEIGHT;

        return roundToTwoDecimals(clamp(weightedScore) * 100);
    }

    public DelayRiskLevel classifyDelayRisk(double delayRiskScore) {
        if (delayRiskScore >= 0.7) {
            return DelayRiskLevel.HIGH;
        }
        if (delayRiskScore >= 0.35) {
            return DelayRiskLevel.MEDIUM;
        }
        return DelayRiskLevel.LOW;
    }

    private String buildReason(RecommendationFeatures features, double priorityScore, DelayRiskLevel delayRisk) {
        List<String> reasons = new ArrayList<>();

        if (features.daysUntilDeadline() <= 0) {
            reasons.add("the deadline is overdue or due today");
        } else if (features.daysUntilDeadline() <= 3) {
            reasons.add("the deadline is coming soon");
        }

        if (features.userPriorityScore() >= 0.9) {
            reasons.add("the task has high user priority");
        }

        if (features.workloadScore() >= 0.9) {
            reasons.add("the estimated workload is high");
        }

        if (features.overdueTaskRatio() >= 0.3) {
            reasons.add("the user has overdue tasks in the current workload");
        }

        if (features.completionRateScore() < 0.5) {
            reasons.add("the user's recent completion rate is low");
        }

        if (reasons.isEmpty()) {
            reasons.add("the task has a balanced urgency and workload profile");
        }

        return "Priority score %.2f with %s delay risk because %s."
                .formatted(priorityScore, delayRisk.name().toLowerCase(), String.join(", ", reasons));
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
