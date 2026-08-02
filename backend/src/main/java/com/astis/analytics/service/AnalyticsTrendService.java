package com.astis.analytics.service;

import com.astis.analytics.dto.AnalyticsTrendResponse;
import com.astis.analytics.dto.DelayedTaskTypeResponse;
import com.astis.analytics.dto.WeeklyAnalyticsResponse;
import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.entity.BehaviorLog;
import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnalyticsTrendService {

    private static final int MIN_WEEKS = 4;
    private static final int MAX_WEEKS = 12;

    private final BehaviorLogRepository behaviorLogRepository;
    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public AnalyticsTrendService(
            BehaviorLogRepository behaviorLogRepository,
            TaskRepository taskRepository,
            AppUserRepository appUserRepository
    ) {
        this.behaviorLogRepository = behaviorLogRepository;
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsTrendResponse getTrends(String userEmail, int weeks) {
        return getTrends(userEmail, weeks, LocalDateTime.now());
    }

    AnalyticsTrendResponse getTrends(String userEmail, int weeks, LocalDateTime generatedAt) {
        validateWeeks(weeks);

        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        LocalDate currentWeekStart = startOfWeek(generatedAt.toLocalDate());
        LocalDate startDate = currentWeekStart.minusWeeks(weeks - 1L);
        LocalDate endDate = currentWeekStart.plusDays(6);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        List<BehaviorLog> completionEvents = behaviorLogRepository
                .findByUserIdAndActionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                        user.getId(),
                        BehaviorActionType.COMPLETE_TASK,
                        startTime,
                        endTime
                );
        List<Task> tasks = taskRepository.findByUserIdAndDeadlineGreaterThanEqualAndDeadlineLessThan(
                user.getId(),
                startTime,
                endTime
        );

        Map<LocalDate, WeeklyCounts> weeklyCounts = createEmptyWeeks(startDate, weeks);
        completionEvents.forEach(event -> incrementCompletion(weeklyCounts, event));

        Map<String, Long> delayedTaskTypeCounts = new LinkedHashMap<>();
        tasks.stream()
                .filter(task -> isOverdue(task, generatedAt))
                .forEach(task -> {
                    LocalDate weekStart = startOfWeek(task.getDeadline().toLocalDate());
                    WeeklyCounts counts = weeklyCounts.get(weekStart);
                    if (counts != null) {
                        counts.overdueCount++;
                        delayedTaskTypeCounts.merge(task.getTaskType(), 1L, Long::sum);
                    }
                });

        List<WeeklyAnalyticsResponse> weeklyTrends = weeklyCounts.entrySet().stream()
                .map(entry -> new WeeklyAnalyticsResponse(
                        entry.getKey(),
                        entry.getKey().plusDays(6),
                        entry.getValue().completedCount,
                        entry.getValue().overdueCount
                ))
                .toList();

        return new AnalyticsTrendResponse(
                generatedAt,
                startDate,
                endDate,
                weeks,
                weeklyTrends,
                findMostDelayedTaskType(delayedTaskTypeCounts),
                roundAverage(taskRepository.findAverageEstimatedHoursByUserId(user.getId()))
        );
    }

    private void validateWeeks(int weeks) {
        if (weeks < MIN_WEEKS || weeks > MAX_WEEKS) {
            throw new IllegalArgumentException("Analytics weeks must be between 4 and 12");
        }
    }

    private Map<LocalDate, WeeklyCounts> createEmptyWeeks(LocalDate startDate, int weeks) {
        Map<LocalDate, WeeklyCounts> weeklyCounts = new LinkedHashMap<>();
        for (int index = 0; index < weeks; index++) {
            weeklyCounts.put(startDate.plusWeeks(index), new WeeklyCounts());
        }
        return weeklyCounts;
    }

    private void incrementCompletion(Map<LocalDate, WeeklyCounts> weeklyCounts, BehaviorLog event) {
        LocalDate weekStart = startOfWeek(event.getCreatedAt().toLocalDate());
        WeeklyCounts counts = weeklyCounts.get(weekStart);
        if (counts != null) {
            counts.completedCount++;
        }
    }

    private boolean isOverdue(Task task, LocalDateTime generatedAt) {
        if (!task.getDeadline().isBefore(generatedAt)) {
            return false;
        }
        return task.getStatus() != TaskStatus.COMPLETED
                || task.getCompletedAt() == null
                || task.getCompletedAt().isAfter(task.getDeadline());
    }

    private DelayedTaskTypeResponse findMostDelayedTaskType(Map<String, Long> delayedTaskTypeCounts) {
        return delayedTaskTypeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new DelayedTaskTypeResponse(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElse(null);
    }

    private double roundAverage(Double averageEstimatedHours) {
        if (averageEstimatedHours == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(averageEstimatedHours)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static final class WeeklyCounts {

        private long completedCount;
        private long overdueCount;
    }
}
