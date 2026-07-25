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

    public RecommendationScore scoreTask(RecommendationFeatures features) {
        double priorityScore = calculatePriorityScore(features);
        DelayRiskLevel delayRisk = classifyDelayRisk(features.delayRiskScore());
        List<String> explanationFactors = buildExplanationFactors(features);
        return new RecommendationScore(
                features.taskId(),
                priorityScore,
                delayRisk,
                buildReason(priorityScore, delayRisk, explanationFactors),
                explanationFactors,
                buildDelayRiskReason(features, delayRisk)
        );
    }

    public double calculatePriorityScore(RecommendationFeatures features) {
        double urgencySignal = (features.urgencyScore() + features.timeDecayScore()) / 2.0;
        double weightedScore = urgencySignal * 0.20
                + features.userPriorityScore() * 0.15
                + features.workloadScore() * 0.10
                + features.delayRiskScore() * 0.10
                + features.completionRateScore() * 0.10
                + features.gradeWeightScore() * 0.15
                + features.difficultyScore() * 0.10
                + features.deadlineFlexibilityScore() * 0.05
                + features.personalImportanceScore() * 0.05;

        if (features.daysUntilDeadline() < 0) {
            weightedScore *= overdueRecoveryFactor(features.deadlineFlexibilityScore());
        }

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

    private List<String> buildExplanationFactors(RecommendationFeatures features) {
        List<String> reasons = new ArrayList<>();

        if (features.daysUntilDeadline() < 0) {
            if (features.deadlineFlexibilityScore() <= 0.4) {
                reasons.add("the task is overdue but may still be recoverable because the deadline is flexible");
            } else {
                reasons.add("the task is overdue, so its ranking is reduced unless late submission is possible");
            }
        } else if (features.daysUntilDeadline() == 0) {
            reasons.add("the deadline is due today");
        } else if (features.daysUntilDeadline() <= 3) {
            reasons.add("the deadline is coming soon");
        }

        if (features.userPriorityScore() >= 0.9) {
            reasons.add("the task has high user priority");
        }

        if (features.workloadScore() >= 0.9) {
            reasons.add("the estimated workload is high");
        }

        if (features.gradeWeightScore() >= 0.8) {
            reasons.add("the task has high grade impact");
        }

        if (features.difficultyScore() >= 0.8) {
            reasons.add("the task is marked as difficult");
        }

        if (features.deadlineFlexibilityScore() >= 0.8) {
            reasons.add("the deadline is not flexible");
        }

        if (features.personalImportanceScore() >= 0.8) {
            reasons.add("the task is personally important");
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

        return List.copyOf(reasons);
    }

    private String buildReason(double priorityScore, DelayRiskLevel delayRisk, List<String> explanationFactors) {
        String keyFactors = explanationFactors.stream()
                .limit(3)
                .reduce((first, second) -> first + ", " + second)
                .orElse("the task has a balanced urgency and workload profile");

        return "Priority score %.2f with %s delay risk. Key factors: %s."
                .formatted(priorityScore, delayRisk.name().toLowerCase(), keyFactors);
    }

    private String buildDelayRiskReason(RecommendationFeatures features, DelayRiskLevel delayRisk) {
        if (features.daysUntilDeadline() < 0) {
            return features.deadlineFlexibilityScore() <= 0.4
                    ? "This task is overdue, but its flexible deadline may still allow recovery."
                    : "This task is overdue, so the ranking is reduced unless late submission remains possible.";
        }
        if (delayRisk == DelayRiskLevel.HIGH) {
            return "High delay risk is driven by the current workload and overdue-task pattern.";
        }
        if (delayRisk == DelayRiskLevel.MEDIUM) {
            return "Moderate delay risk reflects the deadline and current workload pattern.";
        }
        return "Low delay risk based on the current deadline and workload pattern.";
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double overdueRecoveryFactor(double deadlineFlexibilityScore) {
        double flexibilitySignal = 1.0 - deadlineFlexibilityScore;
        return 0.35 + (clamp(flexibilitySignal) * 0.45);
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
