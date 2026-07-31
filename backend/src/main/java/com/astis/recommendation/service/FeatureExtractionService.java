package com.astis.recommendation.service;

import com.astis.recommendation.model.RecommendationFeatures;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.task.repository.UserTaskStatistics;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FeatureExtractionService {

    private static final double DEFAULT_COMPLETION_RATE = 0.5;

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public FeatureExtractionService(TaskRepository taskRepository, AppUserRepository appUserRepository) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationFeatures extractForTask(String userEmail, Long taskId) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        LocalDateTime referenceTime = LocalDateTime.now();
        UserTaskStatistics taskStatistics = taskRepository.findUserTaskStatistics(
                user.getId(),
                TaskStatus.COMPLETED,
                referenceTime
        );

        return extractForTask(user, task, taskStatistics, referenceTime);
    }

    RecommendationFeatures extractForTask(
            AppUser user,
            Task task,
            UserTaskStatistics taskStatistics,
            LocalDateTime referenceTime
    ) {
        long totalTaskCount = taskStatistics.totalTaskCount();
        long completedTaskCount = taskStatistics.completedTaskCount();
        long overdueTaskCount = taskStatistics.overdueTaskCount();

        long daysUntilDeadline = daysUntilDeadline(task.getDeadline(), referenceTime);
        double estimatedHours = estimatedHours(task.getEstimatedHours());
        double completionRate = totalTaskCount == 0
                ? DEFAULT_COMPLETION_RATE
                : (double) completedTaskCount / totalTaskCount;
        double overdueTaskRatio = totalTaskCount == 0
                ? 0.0
                : (double) overdueTaskCount / totalTaskCount;

        return new RecommendationFeatures(
                task.getId(),
                user.getId(),
                daysUntilDeadline,
                estimatedHours,
                urgencyScore(daysUntilDeadline),
                completionRate,
                priorityScore(task.getPriority()),
                timeDecayScore(daysUntilDeadline),
                delayRiskScore(task, overdueTaskRatio, referenceTime),
                workloadScore(estimatedHours),
                criteriaScore(task.getGradeWeight()),
                criteriaScore(task.getDifficultyLevel()),
                deadlineFlexibilityScore(task.getDeadlineFlexibility()),
                criteriaScore(task.getPersonalImportance()),
                overdueTaskRatio
        );
    }

    public RecommendationFeatures defaultFeatures() {
        return new RecommendationFeatures(
                null,
                null,
                0,
                0.0,
                0.0,
                DEFAULT_COMPLETION_RATE,
                0.0,
                0.0,
                0.0,
                0.0,
                0.6,
                0.6,
                0.6,
                0.6,
                0.0
        );
    }

    private long daysUntilDeadline(LocalDateTime deadline, LocalDateTime referenceTime) {
        return ChronoUnit.DAYS.between(referenceTime, deadline);
    }

    private double estimatedHours(BigDecimal estimatedHours) {
        return estimatedHours == null ? 0.0 : estimatedHours.doubleValue();
    }

    private double urgencyScore(long daysUntilDeadline) {
        if (daysUntilDeadline <= 0) {
            return 1.0;
        }
        if (daysUntilDeadline <= 1) {
            return 0.9;
        }
        if (daysUntilDeadline <= 3) {
            return 0.75;
        }
        if (daysUntilDeadline <= 7) {
            return 0.5;
        }
        if (daysUntilDeadline <= 14) {
            return 0.25;
        }
        return 0.1;
    }

    private double priorityScore(TaskPriority priority) {
        return switch (priority) {
            case HIGH -> 1.0;
            case MEDIUM -> 0.6;
            case LOW -> 0.3;
        };
    }

    private double timeDecayScore(long daysUntilDeadline) {
        if (daysUntilDeadline <= 0) {
            return 1.0;
        }
        return Math.max(0.0, 1.0 - (daysUntilDeadline / 14.0));
    }

    private double delayRiskScore(Task task, double overdueTaskRatio, LocalDateTime referenceTime) {
        double currentTaskRisk = task.getDeadline().isBefore(referenceTime)
                && task.getStatus() != TaskStatus.COMPLETED ? 1.0 : 0.0;
        return Math.min(1.0, (currentTaskRisk * 0.6) + (overdueTaskRatio * 0.4));
    }

    private double workloadScore(double estimatedHours) {
        if (estimatedHours <= 0.0) {
            return 0.0;
        }
        if (estimatedHours <= 2.0) {
            return 0.3;
        }
        if (estimatedHours <= 5.0) {
            return 0.6;
        }
        return 1.0;
    }

    private double criteriaScore(int value) {
        return Math.max(1, Math.min(5, value)) / 5.0;
    }

    private double deadlineFlexibilityScore(int value) {
        int clampedValue = Math.max(1, Math.min(5, value));
        return (6 - clampedValue) / 5.0;
    }
}
