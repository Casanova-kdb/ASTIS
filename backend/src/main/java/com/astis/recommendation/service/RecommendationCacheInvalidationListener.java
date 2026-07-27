package com.astis.recommendation.service;

import com.astis.config.CacheNames;
import com.astis.task.event.TaskChangedEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RecommendationCacheInvalidationListener {

    @CacheEvict(cacheNames = CacheNames.USER_RECOMMENDATIONS, key = "#event.userId()")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void removeStaleRecommendations(TaskChangedEvent event) {
        // Cache eviction is performed by Spring after the task transaction commits.
    }
}
