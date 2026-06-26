CREATE DATABASE IF NOT EXISTS astis
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE astis;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(80),
    preferred_study_time VARCHAR(30),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    task_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    deadline DATETIME NOT NULL,
    estimated_hours DECIMAL(5,2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_tasks_status
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_tasks_estimated_hours
        CHECK (estimated_hours IS NULL OR estimated_hours >= 0)
);

CREATE TABLE IF NOT EXISTS behavior_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_behavior_logs_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_behavior_logs_task
        FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_behavior_logs_action_type
        CHECK (action_type IN (
            'CREATE_TASK',
            'UPDATE_TASK',
            'UPDATE_DEADLINE',
            'UPDATE_PRIORITY',
            'UPDATE_STATUS',
            'COMPLETE_TASK',
            'DELETE_TASK',
            'UPDATE_PROFILE',
            'VIEW_RECOMMENDATIONS'
        ))
);

CREATE TABLE IF NOT EXISTS recommendation_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_id BIGINT,
    priority_score DECIMAL(6,2) NOT NULL,
    delay_risk VARCHAR(20) NOT NULL,
    reason TEXT,
    rank_position INT NOT NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_results_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_results_task
        FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_recommendation_results_delay_risk
        CHECK (delay_risk IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_recommendation_results_priority_score
        CHECK (priority_score >= 0),
    CONSTRAINT chk_recommendation_results_rank_position
        CHECK (rank_position > 0)
);

CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_deadline ON tasks (deadline);
CREATE INDEX idx_tasks_user_status_deadline ON tasks (user_id, status, deadline);

CREATE INDEX idx_behavior_logs_user_id ON behavior_logs (user_id);
CREATE INDEX idx_behavior_logs_task_id ON behavior_logs (task_id);
CREATE INDEX idx_behavior_logs_action_type ON behavior_logs (action_type);
CREATE INDEX idx_behavior_logs_created_at ON behavior_logs (created_at);

CREATE INDEX idx_recommendation_results_user_id ON recommendation_results (user_id);
CREATE INDEX idx_recommendation_results_task_id ON recommendation_results (task_id);
CREATE INDEX idx_recommendation_results_generated_at ON recommendation_results (generated_at);
CREATE INDEX idx_recommendation_results_user_generated_at
    ON recommendation_results (user_id, generated_at);
