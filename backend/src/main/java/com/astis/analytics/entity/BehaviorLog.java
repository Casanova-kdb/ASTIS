package com.astis.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "behavior_logs")
public class BehaviorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id")
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private BehaviorActionType actionType;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected BehaviorLog() {
    }

    public BehaviorLog(
            Long userId,
            Long taskId,
            BehaviorActionType actionType,
            String oldValue,
            String newValue
    ) {
        this.userId = userId;
        this.taskId = taskId;
        this.actionType = actionType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public BehaviorActionType getActionType() {
        return actionType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
