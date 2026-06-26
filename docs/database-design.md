# Database Design

## Overview

ASTIS uses MySQL as the MVP relational database.

The database design supports:

- User authentication and profile personalisation
- Academic task management
- Behaviour logging for analytics and recommendation
- Optional persistence of generated recommendation results

The executable SQL script is stored at:

```text
backend/src/main/resources/db/schema.sql
```

Run it locally with:

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

The command will create the `astis` database if it does not already exist.

## Tables

### users

Stores user account and profile information.

Important fields:

- `email`: unique login identifier
- `password_hash`: hashed password value
- `preferred_study_time`: optional personalisation signal for recommendations

### tasks

Stores academic task data created by users.

Important fields:

- `deadline`: used for urgency calculation
- `priority`: user-defined priority, one of `LOW`, `MEDIUM`, `HIGH`
- `status`: one of `PENDING`, `IN_PROGRESS`, `COMPLETED`
- `estimated_hours`: used as a workload signal for recommendation
- `completed_at`: used for completion and delay analysis

### behavior_logs

Stores user actions related to tasks and profile/recommendation behaviour.

Important fields:

- `action_type`: event type, such as `CREATE_TASK`, `UPDATE_DEADLINE`, or `COMPLETE_TASK`
- `old_value` and `new_value`: record before/after values for important updates
- `task_id`: nullable so non-task actions such as profile updates can also be logged

### recommendation_results

Stores generated recommendation results when recommendation history is needed.

This table is optional for the MVP because recommendations can also be calculated dynamically from current task and behaviour data.

Important fields:

- `priority_score`: generated task score
- `delay_risk`: one of `LOW`, `MEDIUM`, `HIGH`
- `reason`: explanation shown to the user
- `rank_position`: task rank in the recommendation result

## Relationships

```text
users 1 -> many tasks
users 1 -> many behavior_logs
tasks 1 -> many behavior_logs
users 1 -> many recommendation_results
tasks 1 -> many recommendation_results
```

## Indexing

Indexes are added for common query patterns:

- tasks by user, status, and deadline
- behavior logs by user, task, action type, and timestamp
- recommendation results by user, task, and generation time
