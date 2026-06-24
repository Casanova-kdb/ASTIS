package com.astis.recommendation.service;

import com.astis.recommendation.model.RecommendationFeatures;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    public double calculatePriorityScore(RecommendationFeatures features) {
        return features.urgencyScore() * 0.35
                + features.completionRateScore() * 0.20
                + features.userPriorityScore() * 0.25
                + features.timeDecayScore() * 0.10
                + features.delayRiskScore() * 0.10;
    }
}
