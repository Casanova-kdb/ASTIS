package com.astis.analytics.service;

import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.entity.BehaviorLog;
import com.astis.analytics.repository.BehaviorLogRepository;
import org.springframework.stereotype.Service;

@Service
public class BehaviorLogService {

    private final BehaviorLogRepository behaviorLogRepository;

    public BehaviorLogService(BehaviorLogRepository behaviorLogRepository) {
        this.behaviorLogRepository = behaviorLogRepository;
    }

    public void recordTaskAction(
            Long userId,
            Long taskId,
            BehaviorActionType actionType,
            String oldValue,
            String newValue
    ) {
        behaviorLogRepository.save(new BehaviorLog(userId, taskId, actionType, oldValue, newValue));
    }
}
