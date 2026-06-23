# Phase 2: System Design

## Architecture Overview

ASTIS will follow a layered backend architecture. The first implementation will focus on a Spring Boot backend and REST API, with a frontend added later if needed.

```text
Frontend / API Client
        |
        v
Controller Layer
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
Database
```

The AI recommendation module will interact with task and behaviour data through the service layer. This keeps the recommendation logic separate from basic CRUD operations.

## Planned Backend Layers

### Controller Layer

Responsible for receiving HTTP requests and returning API responses.

### Service Layer

Responsible for business logic and coordination between modules.

### Repository Layer

Responsible for database access through Spring Data JPA.

## Core Modules

### User Module

Handles user registration, login, authentication, and user profile data.

### Task Module

Handles task creation, updates, deletion, status tracking, deadline tracking, and task retrieval.

### Analytics Module

Collects user behaviour data, including task creation, updates, completion, delay, and deletion events.

### AI Recommendation Module

Calculates task priority scores and returns recommended task ordering based on task urgency, user-defined priority, and historical behaviour.

## Initial Data Design

### User

Stores account and authentication-related data.

Planned fields:

- `id`
- `username`
- `email`
- `passwordHash`
- `createdAt`

### Task

Stores academic task information.

Planned fields:

- `id`
- `userId`
- `title`
- `description`
- `taskType`
- `deadline`
- `priority`
- `status`
- `createdAt`
- `updatedAt`
- `completedAt`

### BehaviorLog

Stores user interaction records for later analytics and recommendation.

Planned fields:

- `id`
- `userId`
- `taskId`
- `actionType`
- `oldValue`
- `newValue`
- `createdAt`

## Data Flow

1. A user creates or updates a task.
2. The controller receives the request and validates input.
3. The service layer applies business logic.
4. The repository layer stores or retrieves data from the database.
5. The analytics module records the user action.
6. The recommendation module calculates task priority scores when requested.
7. The API returns a ranked task list to the user.

## AI Scoring Concept

The MVP recommendation engine will use an explainable scoring formula:

```text
score = urgencyScore * 0.4
      + userPriorityScore * 0.3
      + historyCompletionScore * 0.2
      + delayRiskScore * 0.1
```

This approach is simple, transparent, and suitable for an early-stage personal project. A more advanced model can be introduced later if enough behaviour data is collected.
