package com.astis.studyplan.service;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.settings.entity.DailyStudyCapacity;
import com.astis.settings.entity.PreferredStudyTime;
import com.astis.studyplan.dto.DailyStudyPlanResponse;
import com.astis.studyplan.dto.StudyPlanResponse;
import com.astis.studyplan.dto.StudyPlanWarningResponse;
import com.astis.studyplan.dto.StudySessionResponse;
import com.astis.studyplan.dto.UnscheduledTaskResponse;
import com.astis.studyplan.model.DailyCapacityPolicy;
import com.astis.studyplan.model.PlannedSession;
import com.astis.studyplan.model.StudyPlanWarningCode;
import com.astis.studyplan.model.UnscheduledReason;
import com.astis.task.entity.TaskStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class StudyPlanGeneratorService {

    static final int DEFAULT_PLANNING_DAYS = 7;
    static final int MIN_PLANNING_DAYS = 1;
    static final int MAX_PLANNING_DAYS = 14;

    private static final BigDecimal DEFAULT_ESTIMATED_HOURS = new BigDecimal("1.00");
    private static final BigDecimal MAX_SESSION_HOURS = new BigDecimal("2.00");
    private static final int SESSION_BREAK_MINUTES = 30;
    private static final int LATEST_END_MINUTES = (23 * 60) + 30;

    public StudyPlanResponse generate(
            List<RecommendedTaskResponse> recommendations,
            DailyStudyCapacity studyCapacity,
            PreferredStudyTime preferredStudyTime,
            LocalDateTime generatedAt,
            int planningDays
    ) {
        Objects.requireNonNull(generatedAt, "generatedAt is required");
        validatePlanningDays(planningDays);

        PreferredStudyTime effectiveStudyTime = preferredStudyTime == null
                ? PreferredStudyTime.EVENING
                : preferredStudyTime;
        BigDecimal dailyCapacity = DailyCapacityPolicy.hoursFor(studyCapacity);
        LocalDate startDate = generatedAt.toLocalDate();
        LocalDate endDate = startDate.plusDays(planningDays - 1L);
        List<MutableDayPlan> dayPlans = createDayPlans(
                startDate,
                planningDays,
                dailyCapacity,
                effectiveStudyTime,
                generatedAt
        );
        List<StudyPlanWarningResponse> warnings = new ArrayList<>();
        List<UnscheduledTaskResponse> unscheduledTasks = new ArrayList<>();

        activeTasksInRankOrder(recommendations).forEach(task -> scheduleTask(
                task,
                generatedAt,
                endDate,
                dayPlans,
                warnings,
                unscheduledTasks
        ));

        List<DailyStudyPlanResponse> dailyResponses = dayPlans.stream()
                .map(MutableDayPlan::toResponse)
                .toList();
        BigDecimal totalScheduledHours = dailyResponses.stream()
                .map(DailyStudyPlanResponse::totalScheduledHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalUnscheduledHours = unscheduledTasks.stream()
                .map(UnscheduledTaskResponse::remainingHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean overloaded = unscheduledTasks.stream()
                .anyMatch(task -> task.reason() != UnscheduledReason.OUTSIDE_PLANNING_WINDOW);

        return new StudyPlanResponse(
                generatedAt,
                startDate,
                endDate,
                planningDays,
                effectiveStudyTime,
                dailyCapacity,
                scale(dailyCapacity.multiply(BigDecimal.valueOf(planningDays))),
                scale(totalScheduledHours),
                scale(totalUnscheduledHours),
                overloaded,
                List.copyOf(warnings),
                dailyResponses,
                List.copyOf(unscheduledTasks)
        );
    }

    private void validatePlanningDays(int planningDays) {
        if (planningDays < MIN_PLANNING_DAYS || planningDays > MAX_PLANNING_DAYS) {
            throw new IllegalArgumentException("Planning days must be between 1 and 14");
        }
    }

    private List<RecommendedTaskResponse> activeTasksInRankOrder(
            List<RecommendedTaskResponse> recommendations
    ) {
        if (recommendations == null) {
            return List.of();
        }

        return recommendations.stream()
                .filter(Objects::nonNull)
                .filter(task -> task.status() != TaskStatus.COMPLETED)
                .sorted(Comparator
                        .comparingInt(RecommendedTaskResponse::rankPosition)
                        .thenComparing(RecommendedTaskResponse::deadline))
                .toList();
    }

    private List<MutableDayPlan> createDayPlans(
            LocalDate startDate,
            int planningDays,
            BigDecimal dailyCapacity,
            PreferredStudyTime preferredStudyTime,
            LocalDateTime generatedAt
    ) {
        List<MutableDayPlan> days = new ArrayList<>(planningDays);
        for (int offset = 0; offset < planningDays; offset++) {
            days.add(new MutableDayPlan(
                    startDate.plusDays(offset),
                    dailyCapacity,
                    preferredStudyTime,
                    generatedAt
            ));
        }
        return days;
    }

    private void scheduleTask(
            RecommendedTaskResponse task,
            LocalDateTime generatedAt,
            LocalDate endDate,
            List<MutableDayPlan> dayPlans,
            List<StudyPlanWarningResponse> warnings,
            List<UnscheduledTaskResponse> unscheduledTasks
    ) {
        BigDecimal remainingHours = estimatedHours(task, warnings);
        LocalDateTime deadline = task.deadline();

        if (deadline.isBefore(generatedAt)) {
            addUnscheduled(
                    task,
                    remainingHours,
                    UnscheduledReason.DEADLINE_PASSED,
                    "The deadline has already passed.",
                    warnings,
                    unscheduledTasks
            );
            return;
        }

        for (MutableDayPlan day : dayPlans) {
            if (remainingHours.signum() == 0 || day.date().isAfter(deadline.toLocalDate())) {
                break;
            }

            BigDecimal availableBeforeDeadline = day.availableBefore(deadline);
            while (remainingHours.signum() > 0 && availableBeforeDeadline.signum() > 0) {
                BigDecimal sessionHours = minimum(
                        remainingHours,
                        MAX_SESSION_HOURS,
                        availableBeforeDeadline
                );
                day.addSession(new PlannedSession(
                        task.taskId(),
                        task.title(),
                        sessionHours,
                        deadline,
                        task.priorityScore(),
                        task.delayRisk()
                ));
                remainingHours = scale(remainingHours.subtract(sessionHours));
                availableBeforeDeadline = day.availableBefore(deadline);
            }
        }

        if (remainingHours.signum() > 0) {
            boolean deadlineWithinWindow = !deadline.toLocalDate().isAfter(endDate);
            UnscheduledReason reason = deadlineWithinWindow
                    ? UnscheduledReason.INSUFFICIENT_CAPACITY
                    : UnscheduledReason.OUTSIDE_PLANNING_WINDOW;
            String message = deadlineWithinWindow
                    ? "The remaining work does not fit before the task deadline."
                    : "The remaining work falls outside the selected planning window.";
            addUnscheduled(task, remainingHours, reason, message, warnings, unscheduledTasks);
        }
    }

    private BigDecimal estimatedHours(
            RecommendedTaskResponse task,
            List<StudyPlanWarningResponse> warnings
    ) {
        if (task.estimatedHours() != null && task.estimatedHours().signum() > 0) {
            return scale(task.estimatedHours());
        }

        warnings.add(new StudyPlanWarningResponse(
                StudyPlanWarningCode.DEFAULT_ESTIMATE_USED,
                task.taskId(),
                "A default estimate of one hour was used for this task."
        ));
        return DEFAULT_ESTIMATED_HOURS;
    }

    private void addUnscheduled(
            RecommendedTaskResponse task,
            BigDecimal remainingHours,
            UnscheduledReason reason,
            String message,
            List<StudyPlanWarningResponse> warnings,
            List<UnscheduledTaskResponse> unscheduledTasks
    ) {
        unscheduledTasks.add(new UnscheduledTaskResponse(
                task.taskId(),
                task.title(),
                scale(remainingHours),
                reason
        ));
        warnings.add(new StudyPlanWarningResponse(
                warningCodeFor(reason),
                task.taskId(),
                message
        ));
    }

    private StudyPlanWarningCode warningCodeFor(UnscheduledReason reason) {
        return switch (reason) {
            case DEADLINE_PASSED -> StudyPlanWarningCode.DEADLINE_PASSED;
            case INSUFFICIENT_CAPACITY -> StudyPlanWarningCode.INSUFFICIENT_CAPACITY;
            case OUTSIDE_PLANNING_WINDOW -> StudyPlanWarningCode.OUTSIDE_PLANNING_WINDOW;
        };
    }

    private BigDecimal minimum(BigDecimal first, BigDecimal second, BigDecimal third) {
        return first.min(second).min(third);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static LocalTime preferredStartTime(PreferredStudyTime preferredStudyTime) {
        return switch (preferredStudyTime) {
            case MORNING -> LocalTime.of(9, 0);
            case AFTERNOON -> LocalTime.of(14, 0);
            case EVENING -> LocalTime.of(18, 0);
            case NIGHT -> LocalTime.of(20, 0);
            case FLEXIBLE -> LocalTime.of(10, 0);
        };
    }

    private static final class MutableDayPlan {

        private final LocalDate date;
        private final BigDecimal dailyCapacity;
        private final int startMinutes;
        private final List<PlannedSession> sessions = new ArrayList<>();
        private BigDecimal scheduledHours = BigDecimal.ZERO.setScale(2);

        private MutableDayPlan(
                LocalDate date,
                BigDecimal dailyCapacity,
                PreferredStudyTime preferredStudyTime,
                LocalDateTime generatedAt
        ) {
            this.date = date;
            this.dailyCapacity = dailyCapacity;
            int expectedSessionCount = dailyCapacity
                    .divide(MAX_SESSION_HOURS, 0, RoundingMode.CEILING)
                    .intValue();
            int expectedBreakMinutes = Math.max(0, expectedSessionCount - 1)
                    * SESSION_BREAK_MINUTES;
            int fullCapacityMinutes = toMinutes(dailyCapacity) + expectedBreakMinutes;
            int preferredStartMinutes = preferredStartTime(preferredStudyTime).toSecondOfDay() / 60;
            int capacityAdjustedStartMinutes = Math.min(
                    preferredStartMinutes,
                    Math.max(0, LATEST_END_MINUTES - fullCapacityMinutes)
            );
            this.startMinutes = date.equals(generatedAt.toLocalDate())
                    ? Math.max(capacityAdjustedStartMinutes, nextHalfHour(generatedAt))
                    : capacityAdjustedStartMinutes;
        }

        private LocalDate date() {
            return date;
        }

        private BigDecimal remainingCapacity() {
            return scale(dailyCapacity.subtract(scheduledHours));
        }

        private BigDecimal availableBefore(LocalDateTime deadline) {
            BigDecimal capacity = remainingCapacity();
            if (date.isAfter(deadline.toLocalDate())) {
                return BigDecimal.ZERO.setScale(2);
            }

            int nextStartMinutes = startMinutes
                    + toMinutes(scheduledHours)
                    + (sessions.isEmpty() ? 0 : sessions.size() * SESSION_BREAK_MINUTES);
            int latestAvailableMinutes = LATEST_END_MINUTES;
            if (date.equals(deadline.toLocalDate())) {
                int deadlineMinutes = (deadline.getHour() * 60) + deadline.getMinute();
                latestAvailableMinutes = Math.min(latestAvailableMinutes, deadlineMinutes);
            }
            int availableMinutes = Math.max(
                    0,
                    latestAvailableMinutes - nextStartMinutes
            );
            BigDecimal timeAvailable = BigDecimal.valueOf(availableMinutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.DOWN);
            return capacity.min(timeAvailable);
        }

        private void addSession(PlannedSession session) {
            sessions.add(session);
            scheduledHours = scale(scheduledHours.add(session.durationHours()));
        }

        private DailyStudyPlanResponse toResponse() {
            int cursorMinutes = startMinutes;
            List<StudySessionResponse> responses = new ArrayList<>(sessions.size());

            for (PlannedSession session : sessions) {
                int durationMinutes = toMinutes(session.durationHours());
                int endMinutes = cursorMinutes + durationMinutes;
                responses.add(new StudySessionResponse(
                        session.taskId(),
                        session.title(),
                        toLocalTime(cursorMinutes),
                        toLocalTime(endMinutes),
                        session.durationHours(),
                        session.deadline(),
                        session.priorityScore(),
                        session.delayRisk()
                ));
                cursorMinutes = endMinutes + SESSION_BREAK_MINUTES;
            }

            return new DailyStudyPlanResponse(
                    date,
                    scale(scheduledHours),
                    List.copyOf(responses)
            );
        }

        private static int toMinutes(BigDecimal durationHours) {
            return durationHours
                    .multiply(BigDecimal.valueOf(60))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }

        private static int nextHalfHour(LocalDateTime time) {
            int minutes = (time.getHour() * 60)
                    + time.getMinute()
                    + (time.getSecond() > 0 || time.getNano() > 0 ? 1 : 0);
            int roundedMinutes = ((minutes + 29) / 30) * 30;
            return Math.min(roundedMinutes, 24 * 60);
        }

        private static LocalTime toLocalTime(int totalMinutes) {
            int boundedMinutes = Math.min(totalMinutes, LATEST_END_MINUTES);
            return LocalTime.of(boundedMinutes / 60, boundedMinutes % 60);
        }
    }
}
