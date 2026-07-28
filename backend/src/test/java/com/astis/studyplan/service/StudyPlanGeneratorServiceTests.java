package com.astis.studyplan.service;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.settings.entity.DailyStudyCapacity;
import com.astis.settings.entity.PreferredStudyTime;
import com.astis.studyplan.dto.StudyPlanResponse;
import com.astis.studyplan.model.StudyPlanWarningCode;
import com.astis.studyplan.model.UnscheduledReason;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyPlanGeneratorServiceTests {

    private static final LocalDateTime GENERATED_AT = LocalDateTime.of(2026, 7, 28, 8, 0);

    private final StudyPlanGeneratorService generator = new StudyPlanGeneratorService();

    @Test
    void splitsTasksAndKeepsRecommendationOrderWithinDailyCapacity() {
        RecommendedTaskResponse secondRankedTask = task(
                2,
                2L,
                "Review security flow",
                TaskStatus.PENDING,
                GENERATED_AT.plusDays(10),
                new BigDecimal("1.00")
        );
        RecommendedTaskResponse firstRankedTask = task(
                1,
                1L,
                "Complete database report",
                TaskStatus.IN_PROGRESS,
                GENERATED_AT.plusDays(8),
                new BigDecimal("4.50")
        );

        StudyPlanResponse plan = generator.generate(
                List.of(secondRankedTask, firstRankedTask),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.EVENING,
                GENERATED_AT,
                2
        );

        assertThat(plan.dailyCapacityHours()).isEqualByComparingTo("3.00");
        assertThat(plan.totalAvailableHours()).isEqualByComparingTo("6.00");
        assertThat(plan.totalScheduledHours()).isEqualByComparingTo("5.50");
        assertThat(plan.totalUnscheduledHours()).isEqualByComparingTo("0.00");
        assertThat(plan.overloaded()).isFalse();
        assertThat(plan.unscheduledTasks()).isEmpty();
        assertThat(plan.days())
                .extracting(day -> day.totalScheduledHours())
                .containsExactly(new BigDecimal("3.00"), new BigDecimal("2.50"));
        assertThat(plan.days().stream()
                .flatMap(day -> day.sessions().stream())
                .map(session -> session.taskId())
                .toList())
                .containsExactly(1L, 1L, 1L, 2L);
        assertThat(plan.days().stream()
                .flatMap(day -> day.sessions().stream())
                .allMatch(session -> session.durationHours().compareTo(new BigDecimal("2.00")) <= 0))
                .isTrue();
    }

    @Test
    void doesNotScheduleACompletedTask() {
        StudyPlanResponse plan = generator.generate(
                List.of(task(
                        1,
                        1L,
                        "Completed reflection",
                        TaskStatus.COMPLETED,
                        GENERATED_AT.plusDays(2),
                        new BigDecimal("2.00")
                )),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.MORNING,
                GENERATED_AT,
                3
        );

        assertThat(plan.totalScheduledHours()).isEqualByComparingTo("0.00");
        assertThat(plan.totalUnscheduledHours()).isEqualByComparingTo("0.00");
        assertThat(plan.warnings()).isEmpty();
        assertThat(plan.days()).allMatch(day -> day.sessions().isEmpty());
    }

    @Test
    void returnsAnOverdueTaskAsUnscheduled() {
        StudyPlanResponse plan = generator.generate(
                List.of(task(
                        1,
                        1L,
                        "Late assignment",
                        TaskStatus.PENDING,
                        GENERATED_AT.minusHours(1),
                        new BigDecimal("2.50")
                )),
                DailyStudyCapacity.HEAVY,
                PreferredStudyTime.FLEXIBLE,
                GENERATED_AT,
                7
        );

        assertThat(plan.totalScheduledHours()).isEqualByComparingTo("0.00");
        assertThat(plan.totalUnscheduledHours()).isEqualByComparingTo("2.50");
        assertThat(plan.overloaded()).isTrue();
        assertThat(plan.unscheduledTasks()).singleElement().satisfies(task -> {
            assertThat(task.taskId()).isEqualTo(1L);
            assertThat(task.reason()).isEqualTo(UnscheduledReason.DEADLINE_PASSED);
        });
        assertThat(plan.warnings())
                .extracting(warning -> warning.code())
                .containsExactly(StudyPlanWarningCode.DEADLINE_PASSED);
    }

    @Test
    void doesNotPlaceAStudySessionAfterTheDeadlineTime() {
        StudyPlanResponse plan = generator.generate(
                List.of(task(
                        1,
                        1L,
                        "Due tonight",
                        TaskStatus.PENDING,
                        GENERATED_AT.withHour(20),
                        new BigDecimal("5.00")
                )),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.EVENING,
                GENERATED_AT,
                1
        );

        assertThat(plan.totalScheduledHours()).isEqualByComparingTo("2.00");
        assertThat(plan.totalUnscheduledHours()).isEqualByComparingTo("3.00");
        assertThat(plan.overloaded()).isTrue();
        assertThat(plan.days().get(0).sessions()).singleElement().satisfies(session -> {
            assertThat(session.startTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(session.endTime()).isEqualTo(LocalTime.of(20, 0));
        });
        assertThat(plan.unscheduledTasks()).singleElement()
                .extracting(task -> task.reason())
                .isEqualTo(UnscheduledReason.INSUFFICIENT_CAPACITY);
    }

    @Test
    void usesOneHourAndReturnsAWarningWhenEstimateIsMissing() {
        StudyPlanResponse plan = generator.generate(
                List.of(task(
                        1,
                        1L,
                        "Unestimated reading",
                        TaskStatus.PENDING,
                        GENERATED_AT.plusDays(2),
                        null
                )),
                DailyStudyCapacity.LIGHT,
                PreferredStudyTime.NIGHT,
                GENERATED_AT,
                1
        );

        assertThat(plan.totalScheduledHours()).isEqualByComparingTo("1.00");
        assertThat(plan.days().get(0).sessions()).singleElement().satisfies(session -> {
            assertThat(session.durationHours()).isEqualByComparingTo("1.00");
            assertThat(session.startTime()).isEqualTo(LocalTime.of(20, 0));
        });
        assertThat(plan.warnings())
                .extracting(warning -> warning.code())
                .containsExactly(StudyPlanWarningCode.DEFAULT_ESTIMATE_USED);
    }

    @Test
    void reportsRemainingWorkOutsideTheSelectedWindowWithoutOverload() {
        StudyPlanResponse plan = generator.generate(
                List.of(task(
                        1,
                        1L,
                        "Long-term project",
                        TaskStatus.PENDING,
                        GENERATED_AT.plusDays(30),
                        new BigDecimal("5.00")
                )),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.AFTERNOON,
                GENERATED_AT,
                1
        );

        assertThat(plan.totalScheduledHours()).isEqualByComparingTo("3.00");
        assertThat(plan.totalUnscheduledHours()).isEqualByComparingTo("2.00");
        assertThat(plan.overloaded()).isFalse();
        assertThat(plan.unscheduledTasks()).singleElement()
                .extracting(task -> task.reason())
                .isEqualTo(UnscheduledReason.OUTSIDE_PLANNING_WINDOW);
        assertThat(plan.warnings())
                .extracting(warning -> warning.code())
                .containsExactly(StudyPlanWarningCode.OUTSIDE_PLANNING_WINDOW);
    }

    @Test
    void mapsProfileCapacityToDifferentDailyStudyHours() {
        RecommendedTaskResponse longTask = task(
                1,
                1L,
                "Prepare coursework",
                TaskStatus.PENDING,
                GENERATED_AT.plusDays(10),
                new BigDecimal("8.00")
        );

        StudyPlanResponse lightPlan = generator.generate(
                List.of(longTask),
                DailyStudyCapacity.LIGHT,
                PreferredStudyTime.FLEXIBLE,
                GENERATED_AT,
                1
        );
        StudyPlanResponse mediumPlan = generator.generate(
                List.of(longTask),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.FLEXIBLE,
                GENERATED_AT,
                1
        );
        StudyPlanResponse heavyPlan = generator.generate(
                List.of(longTask),
                DailyStudyCapacity.HEAVY,
                PreferredStudyTime.FLEXIBLE,
                GENERATED_AT,
                1
        );

        assertThat(lightPlan.totalScheduledHours()).isEqualByComparingTo("1.50");
        assertThat(mediumPlan.totalScheduledHours()).isEqualByComparingTo("3.00");
        assertThat(heavyPlan.totalScheduledHours()).isEqualByComparingTo("5.00");
    }

    @Test
    void doesNotCreateAStudySessionInThePastOnTheFirstDay() {
        LocalDateTime eveningGenerationTime = GENERATED_AT.withHour(18).withMinute(10);

        StudyPlanResponse plan = generator.generate(
                List.of(task(
                        1,
                        1L,
                        "Evening revision",
                        TaskStatus.PENDING,
                        eveningGenerationTime.plusDays(2),
                        new BigDecimal("1.00")
                )),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.MORNING,
                eveningGenerationTime,
                2
        );

        assertThat(plan.days().get(0).sessions()).singleElement().satisfies(session -> {
            assertThat(session.startTime()).isEqualTo(LocalTime.of(18, 30));
            assertThat(session.endTime()).isEqualTo(LocalTime.of(19, 30));
        });
    }

    @Test
    void rejectsPlanningWindowsOutsideSupportedRange() {
        assertThatThrownBy(() -> generator.generate(
                List.of(),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.EVENING,
                GENERATED_AT,
                0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Planning days must be between 1 and 14");

        assertThatThrownBy(() -> generator.generate(
                List.of(),
                DailyStudyCapacity.MEDIUM,
                PreferredStudyTime.EVENING,
                GENERATED_AT,
                15
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Planning days must be between 1 and 14");
    }

    private RecommendedTaskResponse task(
            int rankPosition,
            Long taskId,
            String title,
            TaskStatus status,
            LocalDateTime deadline,
            BigDecimal estimatedHours
    ) {
        return new RecommendedTaskResponse(
                rankPosition,
                taskId,
                title,
                "Assignment",
                TaskPriority.HIGH,
                status,
                deadline,
                estimatedHours,
                80.0,
                "MEDIUM",
                "Test recommendation",
                List.of("test factor"),
                "Test delay-risk reason"
        );
    }
}
