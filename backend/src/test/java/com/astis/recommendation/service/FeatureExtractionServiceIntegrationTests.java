package com.astis.recommendation.service;

import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.recommendation.model.RecommendationFeatures;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FeatureExtractionServiceIntegrationTests {

    @Autowired
    private FeatureExtractionService featureExtractionService;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void extractsRecommendationFeaturesForUserOwnedTask() {
        AppUser user = appUserRepository.save(new AppUser("student", "student@example.com", "password-hash"));
        Task targetTask = taskRepository.save(new Task(
                user,
                "AI Coursework",
                "Finish feature extraction",
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(5),
                BigDecimal.valueOf(6.0)
        ));

        Task completedTask = new Task(
                user,
                "Completed Reading",
                null,
                "READING",
                TaskPriority.LOW,
                LocalDateTime.now().plusDays(5),
                BigDecimal.valueOf(1.0)
        );
        completedTask.updateStatus(TaskStatus.COMPLETED);
        taskRepository.save(completedTask);

        Task overdueTask = new Task(
                user,
                "Late Quiz Prep",
                null,
                "EXAM",
                TaskPriority.MEDIUM,
                LocalDateTime.now().minusDays(1),
                BigDecimal.valueOf(2.0)
        );
        taskRepository.save(overdueTask);

        RecommendationFeatures features = featureExtractionService.extractForTask(
                "student@example.com",
                targetTask.getId()
        );

        assertThat(features.taskId()).isEqualTo(targetTask.getId());
        assertThat(features.userId()).isEqualTo(user.getId());
        assertThat(features.daysUntilDeadline()).isBetween(4L, 5L);
        assertThat(features.estimatedHours()).isEqualTo(6.0);
        assertThat(features.urgencyScore()).isEqualTo(0.5);
        assertThat(features.userPriorityScore()).isEqualTo(1.0);
        assertThat(features.workloadScore()).isEqualTo(1.0);
        assertThat(features.completionRateScore()).isCloseTo(1.0 / 3.0, org.assertj.core.data.Offset.offset(0.001));
        assertThat(features.overdueTaskRatio()).isCloseTo(1.0 / 3.0, org.assertj.core.data.Offset.offset(0.001));
        assertThat(features.delayRiskScore()).isCloseTo(0.133, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void rejectsTaskOwnedByAnotherUser() {
        AppUser owner = appUserRepository.save(new AppUser("owner", "owner@example.com", "password-hash"));
        appUserRepository.save(new AppUser("student", "student@example.com", "password-hash"));
        Task task = taskRepository.save(new Task(
                owner,
                "Private Task",
                null,
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(2),
                null
        ));

        assertThatThrownBy(() -> featureExtractionService.extractForTask("student@example.com", task.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void defaultFeaturesUseNewUserFallbackValues() {
        RecommendationFeatures features = featureExtractionService.defaultFeatures();

        assertThat(features.completionRateScore()).isEqualTo(0.5);
        assertThat(features.overdueTaskRatio()).isEqualTo(0.0);
        assertThat(features.taskId()).isNull();
        assertThat(features.userId()).isNull();
    }
}
