package com.astis.analytics.service;

import com.astis.analytics.dto.AnalyticsTrendResponse;
import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.entity.BehaviorLog;
import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AnalyticsTrendServiceTests {

    private static final String USER_EMAIL = "student@example.com";
    private static final Long USER_ID = 42L;
    private static final LocalDateTime GENERATED_AT = LocalDateTime.of(2026, 8, 1, 12, 0);
    private static final LocalDateTime WINDOW_START = LocalDateTime.of(2026, 7, 6, 0, 0);
    private static final LocalDateTime WINDOW_END = LocalDateTime.of(2026, 8, 3, 0, 0);

    private BehaviorLogRepository behaviorLogRepository;
    private TaskRepository taskRepository;
    private AppUserRepository appUserRepository;
    private AnalyticsTrendService analyticsTrendService;
    private AppUser user;

    @BeforeEach
    void setUp() {
        behaviorLogRepository = mock(BehaviorLogRepository.class);
        taskRepository = mock(TaskRepository.class);
        appUserRepository = mock(AppUserRepository.class);
        analyticsTrendService = new AnalyticsTrendService(
                behaviorLogRepository,
                taskRepository,
                appUserRepository
        );

        user = new AppUser("student", USER_EMAIL, "password-hash");
        ReflectionTestUtils.setField(user, "id", USER_ID);
        when(appUserRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void returnsOrderedZeroFilledWeeksAndGroupsCompletionEvents() {
        when(behaviorLogRepository
                .findByUserIdAndActionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                        USER_ID,
                        BehaviorActionType.COMPLETE_TASK,
                        WINDOW_START,
                        WINDOW_END
                ))
                .thenReturn(List.of(
                        completionEvent(LocalDateTime.of(2026, 7, 7, 9, 0)),
                        completionEvent(LocalDateTime.of(2026, 7, 28, 10, 0)),
                        completionEvent(LocalDateTime.of(2026, 8, 1, 11, 0))
                ));
        when(taskRepository.findByUserIdAndDeadlineGreaterThanEqualAndDeadlineLessThan(
                USER_ID,
                WINDOW_START,
                WINDOW_END
        )).thenReturn(List.of());
        when(taskRepository.findAverageEstimatedHoursByUserId(USER_ID)).thenReturn(2.345);

        AnalyticsTrendResponse response = analyticsTrendService.getTrends(USER_EMAIL, 4, GENERATED_AT);

        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(response.weeklyTrends())
                .extracting(week -> week.weekStart())
                .containsExactly(
                        LocalDate.of(2026, 7, 6),
                        LocalDate.of(2026, 7, 13),
                        LocalDate.of(2026, 7, 20),
                        LocalDate.of(2026, 7, 27)
                );
        assertThat(response.weeklyTrends())
                .extracting(week -> week.completedCount())
                .containsExactly(1L, 0L, 0L, 2L);
        assertThat(response.weeklyTrends())
                .extracting(week -> week.overdueCount())
                .containsExactly(0L, 0L, 0L, 0L);
        assertThat(response.mostDelayedTaskType()).isNull();
        assertThat(response.averageEstimatedHours()).isEqualTo(2.35);

        verify(taskRepository).findByUserIdAndDeadlineGreaterThanEqualAndDeadlineLessThan(
                USER_ID,
                WINDOW_START,
                WINDOW_END
        );
    }

    @Test
    void countsOnlyIncompleteAndLateCompletedTasksAsOverdue() {
        List<Task> tasks = List.of(
                task("COURSEWORK", LocalDateTime.of(2026, 7, 8, 12, 0), TaskStatus.PENDING, null),
                task(
                        "EXAM",
                        LocalDateTime.of(2026, 7, 15, 12, 0),
                        TaskStatus.COMPLETED,
                        LocalDateTime.of(2026, 7, 16, 9, 0)
                ),
                task(
                        "LAB",
                        LocalDateTime.of(2026, 7, 22, 12, 0),
                        TaskStatus.COMPLETED,
                        LocalDateTime.of(2026, 7, 21, 18, 0)
                ),
                task("PRESENTATION", LocalDateTime.of(2026, 8, 2, 18, 0), TaskStatus.PENDING, null)
        );
        stubTrendQueries(List.of(), tasks, 4.0);

        AnalyticsTrendResponse response = analyticsTrendService.getTrends(USER_EMAIL, 4, GENERATED_AT);

        assertThat(response.weeklyTrends())
                .extracting(week -> week.overdueCount())
                .containsExactly(1L, 1L, 0L, 0L);
        assertThat(response.mostDelayedTaskType().taskType()).isEqualTo("COURSEWORK");
        assertThat(response.mostDelayedTaskType().delayedCount()).isEqualTo(1L);
    }

    @Test
    void usesAlphabeticalTaskTypeAsTieBreaker() {
        List<Task> tasks = List.of(
                task("EXAM", LocalDateTime.of(2026, 7, 8, 12, 0), TaskStatus.PENDING, null),
                task("COURSEWORK", LocalDateTime.of(2026, 7, 9, 12, 0), TaskStatus.PENDING, null)
        );
        stubTrendQueries(List.of(), tasks, null);

        AnalyticsTrendResponse response = analyticsTrendService.getTrends(USER_EMAIL, 4, GENERATED_AT);

        assertThat(response.mostDelayedTaskType().taskType()).isEqualTo("COURSEWORK");
        assertThat(response.averageEstimatedHours()).isZero();
    }

    @Test
    void rejectsUnsupportedReportingWindowsBeforeQueryingData() {
        assertThatThrownBy(() -> analyticsTrendService.getTrends(USER_EMAIL, 3, GENERATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Analytics weeks must be between 4 and 12");
        assertThatThrownBy(() -> analyticsTrendService.getTrends(USER_EMAIL, 13, GENERATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Analytics weeks must be between 4 and 12");

        verifyNoInteractions(behaviorLogRepository, taskRepository);
    }

    private void stubTrendQueries(List<BehaviorLog> events, List<Task> tasks, Double averageEstimatedHours) {
        when(behaviorLogRepository
                .findByUserIdAndActionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                        USER_ID,
                        BehaviorActionType.COMPLETE_TASK,
                        WINDOW_START,
                        WINDOW_END
                )).thenReturn(events);
        when(taskRepository.findByUserIdAndDeadlineGreaterThanEqualAndDeadlineLessThan(
                USER_ID,
                WINDOW_START,
                WINDOW_END
        )).thenReturn(tasks);
        when(taskRepository.findAverageEstimatedHoursByUserId(USER_ID)).thenReturn(averageEstimatedHours);
    }

    private BehaviorLog completionEvent(LocalDateTime createdAt) {
        BehaviorLog event = new BehaviorLog(
                USER_ID,
                1L,
                BehaviorActionType.COMPLETE_TASK,
                "IN_PROGRESS",
                "COMPLETED"
        );
        ReflectionTestUtils.setField(event, "createdAt", createdAt);
        return event;
    }

    private Task task(
            String taskType,
            LocalDateTime deadline,
            TaskStatus status,
            LocalDateTime completedAt
    ) {
        Task task = new Task(
                user,
                taskType + " task",
                "Test task",
                taskType,
                TaskPriority.MEDIUM,
                deadline,
                BigDecimal.ONE
        );
        ReflectionTestUtils.setField(task, "status", status);
        ReflectionTestUtils.setField(task, "completedAt", completedAt);
        return task;
    }
}
