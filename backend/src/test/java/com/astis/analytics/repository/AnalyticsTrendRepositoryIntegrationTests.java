package com.astis.analytics.repository;

import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.entity.BehaviorLog;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AnalyticsTrendRepositoryIntegrationTests {

    private static final LocalDateTime WINDOW_START = LocalDateTime.of(2026, 7, 6, 0, 0);
    private static final LocalDateTime WINDOW_END = LocalDateTime.of(2026, 8, 3, 0, 0);

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void appliesUserAndTimeBoundariesToTrendQueries() {
        AppUser user = appUserRepository.save(new AppUser("student", "student@example.com", "password-hash"));
        AppUser otherUser = appUserRepository.save(new AppUser("other", "other@example.com", "password-hash"));

        taskRepository.saveAll(List.of(
                task(user, "In-window task", LocalDateTime.of(2026, 7, 15, 12, 0), "2.00"),
                task(user, "Zero estimate", LocalDateTime.of(2026, 7, 20, 12, 0), "0.00"),
                task(user, "Long-term task", LocalDateTime.of(2026, 9, 1, 12, 0), "6.00"),
                task(otherUser, "Other user's task", LocalDateTime.of(2026, 7, 16, 12, 0), "10.00")
        ));

        behaviorLogRepository.saveAll(List.of(
                behaviorLog(user.getId(), BehaviorActionType.COMPLETE_TASK, LocalDateTime.of(2026, 7, 10, 9, 0)),
                behaviorLog(user.getId(), BehaviorActionType.UPDATE_TASK, LocalDateTime.of(2026, 7, 11, 9, 0)),
                behaviorLog(user.getId(), BehaviorActionType.COMPLETE_TASK, LocalDateTime.of(2026, 6, 30, 9, 0)),
                behaviorLog(otherUser.getId(), BehaviorActionType.COMPLETE_TASK, LocalDateTime.of(2026, 7, 12, 9, 0))
        ));

        List<BehaviorLog> completionEvents = behaviorLogRepository
                .findByUserIdAndActionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                        user.getId(),
                        BehaviorActionType.COMPLETE_TASK,
                        WINDOW_START,
                        WINDOW_END
                );
        List<Task> deadlineTasks = taskRepository.findByUserIdAndDeadlineGreaterThanEqualAndDeadlineLessThan(
                user.getId(),
                WINDOW_START,
                WINDOW_END
        );
        Double averageEstimatedHours = taskRepository.findAverageEstimatedHoursByUserId(user.getId());

        assertThat(completionEvents)
                .extracting(BehaviorLog::getCreatedAt)
                .containsExactly(LocalDateTime.of(2026, 7, 10, 9, 0));
        assertThat(deadlineTasks)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("In-window task", "Zero estimate");
        assertThat(averageEstimatedHours).isEqualTo(4.0);
    }

    private Task task(AppUser user, String title, LocalDateTime deadline, String estimatedHours) {
        return new Task(
                user,
                title,
                "Repository integration test",
                "COURSEWORK",
                TaskPriority.MEDIUM,
                deadline,
                new BigDecimal(estimatedHours)
        );
    }

    private BehaviorLog behaviorLog(
            Long userId,
            BehaviorActionType actionType,
            LocalDateTime createdAt
    ) {
        BehaviorLog behaviorLog = new BehaviorLog(userId, null, actionType, null, null);
        ReflectionTestUtils.setField(behaviorLog, "createdAt", createdAt);
        return behaviorLog;
    }
}
