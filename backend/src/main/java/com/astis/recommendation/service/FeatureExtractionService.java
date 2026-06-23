package com.astis.recommendation.service;

import com.astis.recommendation.model.RecommendationFeatures;
import org.springframework.stereotype.Service;

@Service
public class FeatureExtractionService {

    public RecommendationFeatures defaultFeatures() {
        return new RecommendationFeatures(
                0.0,
                0.5,
                0.0,
                0.0,
                0.0
        );
    }
}
