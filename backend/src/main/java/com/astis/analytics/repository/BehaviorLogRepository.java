package com.astis.analytics.repository;

import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.entity.BehaviorLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BehaviorLogRepository extends JpaRepository<BehaviorLog, Long> {

    List<BehaviorLog> findByUserIdOrderByCreatedAtAsc(Long userId);

    List<BehaviorLog> findByUserIdAndActionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            Long userId,
            BehaviorActionType actionType,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    long countByActionType(BehaviorActionType actionType);
}
