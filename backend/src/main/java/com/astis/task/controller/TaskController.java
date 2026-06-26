package com.astis.task.controller;

import com.astis.common.api.ApiResponse;
import com.astis.task.dto.CreateTaskRequest;
import com.astis.task.dto.TaskResponse;
import com.astis.task.dto.UpdateTaskRequest;
import com.astis.task.dto.UpdateTaskStatusRequest;
import com.astis.task.entity.TaskStatus;
import com.astis.task.service.TaskService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            Principal principal,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(principal.getName(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created", response));
    }

    @GetMapping
    public ApiResponse<List<TaskResponse>> getTasks(
            Principal principal,
            @RequestParam(required = false) TaskStatus status
    ) {
        return ApiResponse.success("Tasks retrieved", taskService.getTasks(principal.getName(), status));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponse> getTask(Principal principal, @PathVariable Long taskId) {
        return ApiResponse.success("Task retrieved", taskService.getTask(principal.getName(), taskId));
    }

    @PutMapping("/{taskId}")
    public ApiResponse<TaskResponse> updateTask(
            Principal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ApiResponse.success("Task updated", taskService.updateTask(principal.getName(), taskId, request));
    }

    @PatchMapping("/{taskId}/status")
    public ApiResponse<TaskResponse> updateTaskStatus(
            Principal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return ApiResponse.success("Task status updated", taskService.updateTaskStatus(principal.getName(), taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(Principal principal, @PathVariable Long taskId) {
        taskService.deleteTask(principal.getName(), taskId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted", null));
    }
}
