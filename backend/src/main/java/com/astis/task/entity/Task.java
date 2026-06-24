package com.astis.task.entity;

import com.astis.user.entity.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(name = "estimated_hours", precision = 5, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected Task() {
    }

    public Task(
            AppUser user,
            String title,
            String description,
            String taskType,
            TaskPriority priority,
            LocalDateTime deadline,
            BigDecimal estimatedHours
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.taskType = taskType;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
        this.deadline = deadline;
        this.estimatedHours = estimatedHours;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void updateDetails(
            String title,
            String description,
            String taskType,
            TaskPriority priority,
            LocalDateTime deadline,
            BigDecimal estimatedHours
    ) {
        this.title = title;
        this.description = description;
        this.taskType = taskType;
        this.priority = priority;
        this.deadline = deadline;
        this.estimatedHours = estimatedHours;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(TaskStatus status) {
        this.status = status;
        this.completedAt = status == TaskStatus.COMPLETED ? LocalDateTime.now() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTaskType() {
        return taskType;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public BigDecimal getEstimatedHours() {
        return estimatedHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
