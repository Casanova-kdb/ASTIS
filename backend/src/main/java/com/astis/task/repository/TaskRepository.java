package com.astis.task.repository;

import com.astis.task.entity.Task;
import com.astis.task.entity.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByDeadlineAsc(Long userId);

    List<Task> findByUserIdAndStatusOrderByDeadlineAsc(Long userId, TaskStatus status);

    List<Task> findByUserIdAndStatusNotOrderByDeadlineAsc(Long userId, TaskStatus status);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, TaskStatus status);

    long countByUserIdAndStatusNotAndDeadlineBefore(Long userId, TaskStatus status, LocalDateTime deadline);
}
