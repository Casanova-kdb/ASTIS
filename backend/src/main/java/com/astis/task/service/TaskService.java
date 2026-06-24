package com.astis.task.service;

import com.astis.task.dto.CreateTaskRequest;
import com.astis.task.dto.TaskResponse;
import com.astis.task.dto.UpdateTaskRequest;
import com.astis.task.dto.UpdateTaskStatusRequest;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public TaskService(TaskRepository taskRepository, AppUserRepository appUserRepository) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
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

        return TaskResponse.from(taskRepository.save(task));
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
        task.updateDetails(
                request.title(),
                request.description(),
                request.taskType(),
                request.priority(),
                request.deadline(),
                request.estimatedHours()
        );

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(String userEmail, Long taskId, UpdateTaskStatusRequest request) {
        AppUser user = findUserByEmail(userEmail);
        Task task = findTaskForUser(taskId, user.getId());
        task.updateStatus(request.status());
        return TaskResponse.from(task);
    }

    @Transactional
    public void deleteTask(String userEmail, Long taskId) {
        AppUser user = findUserByEmail(userEmail);
        Task task = findTaskForUser(taskId, user.getId());
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
}
