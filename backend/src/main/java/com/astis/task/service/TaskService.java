package com.astis.task.service;

import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.service.BehaviorLogService;
import com.astis.task.dto.CreateTaskRequest;
import com.astis.task.dto.TaskResponse;
import com.astis.task.dto.UpdateTaskRequest;
import com.astis.task.dto.UpdateTaskStatusRequest;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;
    private final BehaviorLogService behaviorLogService;

    public TaskService(
            TaskRepository taskRepository,
            AppUserRepository appUserRepository,
            BehaviorLogService behaviorLogService
    ) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
        this.behaviorLogService = behaviorLogService;
    }

    @Transactional
    public TaskResponse createTask(String userEmail, CreateTaskRequest request) {
        AppUser user = findUserByEmail(userEmail);
        Task task = new Task(
                user,
                request.title(),
                request.description(),
                request.taskType(),
                request.priority(),
                request.deadline(),
                request.estimatedHours()
        );
        Task savedTask = taskRepository.save(task);
        behaviorLogService.recordTaskAction(
                user.getId(),
                savedTask.getId(),
                BehaviorActionType.CREATE_TASK,
                null,
                taskSummary(savedTask)
        );

        return TaskResponse.from(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(String userEmail, TaskStatus status) {
        AppUser user = findUserByEmail(userEmail);
        List<Task> tasks = status == null
                ? taskRepository.findByUserIdOrderByDeadlineAsc(user.getId())
                : taskRepository.findByUserIdAndStatusOrderByDeadlineAsc(user.getId(), status);

        return tasks.stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(String userEmail, Long taskId) {
        AppUser user = findUserByEmail(userEmail);
        return TaskResponse.from(findTaskForUser(taskId, user.getId()));
    }

    @Transactional
    public TaskResponse updateTask(String userEmail, Long taskId, UpdateTaskRequest request) {
        AppUser user = findUserByEmail(userEmail);
        Task task = findTaskForUser(taskId, user.getId());
        TaskChangeSnapshot before = TaskChangeSnapshot.from(task);

        task.updateDetails(
                request.title(),
                request.description(),
                request.taskType(),
                request.priority(),
                request.deadline(),
                request.estimatedHours()
        );
        recordTaskDetailChanges(user.getId(), task, before);

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(String userEmail, Long taskId, UpdateTaskStatusRequest request) {
        AppUser user = findUserByEmail(userEmail);
        Task task = findTaskForUser(taskId, user.getId());
        TaskStatus previousStatus = task.getStatus();
        task.updateStatus(request.status());
        recordStatusChange(user.getId(), task, previousStatus);
        return TaskResponse.from(task);
    }

    @Transactional
    public void deleteTask(String userEmail, Long taskId) {
        AppUser user = findUserByEmail(userEmail);
        Task task = findTaskForUser(taskId, user.getId());
        behaviorLogService.recordTaskAction(
                user.getId(),
                task.getId(),
                BehaviorActionType.DELETE_TASK,
                taskSummary(task),
                null
        );
        taskRepository.delete(task);
    }

    private AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private Task findTaskForUser(Long taskId, Long userId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private void recordTaskDetailChanges(Long userId, Task task, TaskChangeSnapshot before) {
        TaskChangeSnapshot after = TaskChangeSnapshot.from(task);

        if (!before.equals(after)) {
            behaviorLogService.recordTaskAction(
                    userId,
                    task.getId(),
                    BehaviorActionType.UPDATE_TASK,
                    before.toLogValue(),
                    after.toLogValue()
            );
        }

        if (!Objects.equals(before.deadline(), after.deadline())) {
            behaviorLogService.recordTaskAction(
                    userId,
                    task.getId(),
                    BehaviorActionType.UPDATE_DEADLINE,
                    String.valueOf(before.deadline()),
                    String.valueOf(after.deadline())
            );
        }

        if (before.priority() != after.priority()) {
            behaviorLogService.recordTaskAction(
                    userId,
                    task.getId(),
                    BehaviorActionType.UPDATE_PRIORITY,
                    String.valueOf(before.priority()),
                    String.valueOf(after.priority())
            );
        }
    }

    private void recordStatusChange(Long userId, Task task, TaskStatus previousStatus) {
        if (previousStatus == task.getStatus()) {
            return;
        }

        behaviorLogService.recordTaskAction(
                userId,
                task.getId(),
                BehaviorActionType.UPDATE_STATUS,
                String.valueOf(previousStatus),
                String.valueOf(task.getStatus())
        );

        if (task.getStatus() == TaskStatus.COMPLETED) {
            behaviorLogService.recordTaskAction(
                    userId,
                    task.getId(),
                    BehaviorActionType.COMPLETE_TASK,
                    String.valueOf(previousStatus),
                    taskSummary(task)
            );
        }
    }

    private String taskSummary(Task task) {
        return "id=%s,title=%s,status=%s,priority=%s,deadline=%s"
                .formatted(task.getId(), task.getTitle(), task.getStatus(), task.getPriority(), task.getDeadline());
    }

    private record TaskChangeSnapshot(
            String title,
            String description,
            String taskType,
            TaskPriority priority,
            LocalDateTime deadline,
            BigDecimal estimatedHours
    ) {
        private static TaskChangeSnapshot from(Task task) {
            return new TaskChangeSnapshot(
                    task.getTitle(),
                    task.getDescription(),
                    task.getTaskType(),
                    task.getPriority(),
                    task.getDeadline(),
                    task.getEstimatedHours()
            );
        }

        private String toLogValue() {
            return "title=%s,taskType=%s,priority=%s,deadline=%s,estimatedHours=%s"
                    .formatted(title, taskType, priority, deadline, estimatedHours);
        }
    }
}
