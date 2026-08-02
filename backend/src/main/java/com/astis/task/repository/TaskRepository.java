package com.astis.task.repository;

import com.astis.task.entity.Task;
import com.astis.task.entity.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByDeadlineAsc(Long userId);

    List<Task> findByUserIdAndStatusOrderByDeadlineAsc(Long userId, TaskStatus status);

    List<Task> findByUserIdAndStatusNotOrderByDeadlineAsc(Long userId, TaskStatus status);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    List<Task> findByUserIdAndDeadlineGreaterThanEqualAndDeadlineLessThan(
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, TaskStatus status);

    long countByUserIdAndStatusNotAndDeadlineBefore(Long userId, TaskStatus status, LocalDateTime deadline);

    @Query("""
            select new com.astis.task.repository.UserTaskStatistics(
                count(task),
                coalesce(sum(case when task.status = :completedStatus then 1 else 0 end), 0),
                coalesce(sum(case when task.status <> :completedStatus and task.deadline < :referenceTime then 1 else 0 end), 0)
            )
            from Task task
            where task.user.id = :userId
            """)
    UserTaskStatistics findUserTaskStatistics(
            @Param("userId") Long userId,
            @Param("completedStatus") TaskStatus completedStatus,
            @Param("referenceTime") LocalDateTime referenceTime
    );

    @Query("""
            select avg(task.estimatedHours)
            from Task task
            where task.user.id = :userId
              and task.estimatedHours is not null
              and task.estimatedHours > 0
            """)
    Double findAverageEstimatedHoursByUserId(@Param("userId") Long userId);
}
