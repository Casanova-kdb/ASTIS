package com.astis.analytics.repository;

import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.entity.BehaviorLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BehaviorLogRepository extends JpaRepository<BehaviorLog, Long> {

    List<BehaviorLog> findByUserIdOrderByCreatedAtAsc(Long userId);

    long countByActionType(BehaviorActionType actionType);
}
