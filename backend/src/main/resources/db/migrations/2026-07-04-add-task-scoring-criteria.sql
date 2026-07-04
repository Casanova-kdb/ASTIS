USE astis;

ALTER TABLE tasks
    ADD COLUMN grade_weight INT NOT NULL DEFAULT 3,
    ADD COLUMN difficulty_level INT NOT NULL DEFAULT 3,
    ADD COLUMN deadline_flexibility INT NOT NULL DEFAULT 3,
    ADD COLUMN personal_importance INT NOT NULL DEFAULT 3;

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_grade_weight CHECK (grade_weight BETWEEN 1 AND 5),
    ADD CONSTRAINT chk_tasks_difficulty_level CHECK (difficulty_level BETWEEN 1 AND 5),
    ADD CONSTRAINT chk_tasks_deadline_flexibility CHECK (deadline_flexibility BETWEEN 1 AND 5),
    ADD CONSTRAINT chk_tasks_personal_importance CHECK (personal_importance BETWEEN 1 AND 5);
