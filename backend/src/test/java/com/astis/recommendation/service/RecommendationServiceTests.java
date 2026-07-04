package com.astis.recommendation.service;

import com.astis.recommendation.model.DelayRiskLevel;
import com.astis.recommendation.model.RecommendationFeatures;
import com.astis.recommendation.model.RecommendationScore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationServiceTests {

    private final RecommendationService recommendationService = new RecommendationService();

    @Test
    void calculatesPriorityScoreFromTaskFeaturesAndTaskCriteria() {
        RecommendationFeatures features = new RecommendationFeatures(
                10L,
                1L,
                2,
                6.0,
                0.75,
                0.50,
                1.00,
                0.80,
                0.40,
                1.00,
                0.60,
                0.60,
                0.60,
                0.60,
                0.30
        );

        double score = recommendationService.calculatePriorityScore(features);

        assertThat(score).isEqualTo(70.5);
    }

    @Test
    void keepsScoreWithinOneHundredWhenAllSignalsAreMaxed() {
        RecommendationFeatures features = new RecommendationFeatures(
                10L,
                1L,
                0,
                8.0,
                1.00,
                1.00,
                1.00,
                1.00,
                1.00,
                1.00,
                1.00,
                1.00,
                1.00,
                1.00,
                0.50
        );

        double score = recommendationService.calculatePriorityScore(features);

        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void classifiesDelayRiskLevels() {
        assertThat(recommendationService.classifyDelayRisk(0.10)).isEqualTo(DelayRiskLevel.LOW);
        assertThat(recommendationService.classifyDelayRisk(0.35)).isEqualTo(DelayRiskLevel.MEDIUM);
        assertThat(recommendationService.classifyDelayRisk(0.70)).isEqualTo(DelayRiskLevel.HIGH);
    }

    @Test
    void createsRecommendationScoreWithTaskCriteriaReasons() {
        RecommendationFeatures features = new RecommendationFeatures(
                10L,
                1L,
                1,
                8.0,
                0.90,
                0.30,
                1.00,
                0.95,
                0.80,
                1.00,
                1.00,
                1.00,
                1.00,
                1.00,
                0.40
        );

        RecommendationScore score = recommendationService.scoreTask(features);

        assertThat(score.taskId()).isEqualTo(10L);
        assertThat(score.priorityScore()).isEqualTo(89.5);
        assertThat(score.delayRisk()).isEqualTo(DelayRiskLevel.HIGH);
        assertThat(score.reason())
                .contains("deadline is coming soon")
                .contains("high user priority")
                .contains("high grade impact")
                .contains("marked as difficult")
                .contains("deadline is not flexible")
                .contains("personally important")
                .contains("high delay risk");
    }
}
