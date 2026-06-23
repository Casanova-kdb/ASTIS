# User Stories

## Purpose

This document defines the MVP user stories for ASTIS.

The goal is to make sure each MVP feature can be traced from user need to acceptance criteria and development tasks. These stories will later be converted into GitHub Issues and implemented through sprint branches.

## MVP Coverage Map

| MVP Capability | Covered By |
| --- | --- |
| User registration and login | US-001, US-002 |
| Authenticated access control | US-003 |
| User profile personalisation data | US-004 |
| Task creation | US-005 |
| Task list and task detail retrieval | US-006, US-007 |
| Task update and status tracking | US-008, US-009 |
| Task deletion | US-010 |
| Behaviour logging | US-011 |
| Basic analytics summary | US-012 |
| AI feature extraction | US-013 |
| AI-based task scoring and delay risk | US-014 |
| Recommended task ordering | US-015 |

## Epic 1: User Management and Security

### US-001: User Registration

As a student, I want to create an account, so that my academic tasks and study behaviour can be stored securely.

Acceptance Criteria:

- The user can register with a username, email, and password.
- Email must be unique.
- Password must not be stored as plain text.
- Invalid registration data returns validation errors.
- A successful registration returns a clear success response.

Development Tasks:

- Design User entity.
- Create User repository.
- Implement registration request and response DTOs.
- Implement `POST /auth/register`.
- Add password hashing.
- Add validation and duplicate email handling.
- Test successful and failed registration cases.

### US-002: User Login

As a student, I want to log in to the system, so that I can access my personal task workspace.

Acceptance Criteria:

- The user can log in with valid email and password.
- Invalid credentials return an authentication error.
- A successful login returns a JWT.
- The returned token can be used to access protected APIs.

Development Tasks:

- Configure Spring Security.
- Implement authentication service.
- Implement `POST /auth/login`.
- Generate JWT after successful login.
- Add login validation and error responses.
- Test login with valid and invalid credentials.

### US-003: Protected Personal Workspace

As a student, I want my task data to be private, so that other users cannot access or modify my study records.

Acceptance Criteria:

- Task APIs require authentication.
- A user can only view their own tasks.
- A user can only update or delete their own tasks.
- Unauthenticated requests are rejected.
- Requests for another user's task are rejected.

Development Tasks:

- Add JWT authentication filter.
- Extract authenticated user identity from token.
- Apply ownership checks in task service methods.
- Add consistent unauthorized and forbidden responses.
- Test protected route access and task ownership rules.

### US-004: Manage User Profile

As a student, I want to manage my study profile, so that the system can personalise recommendations based on my habits and preferences.

Acceptance Criteria:

- The user can view their profile.
- The user can update basic profile fields such as display name.
- The user can store optional study preference data, such as preferred study time.
- Profile data is only accessible to the authenticated user.
- Profile data can be used by the recommendation module when available.

Development Tasks:

- Extend User entity or create UserProfile fields.
- Implement profile request and response DTOs.
- Implement `GET /users/me`.
- Implement `PUT /users/me`.
- Add ownership protection through authenticated identity.
- Test profile retrieval and update cases.

## Epic 2: Task Management

### US-005: Create Academic Task

As a student, I want to create academic tasks with deadlines, so that I can manage my study workload.

Acceptance Criteria:

- The user can create a task with title, deadline, priority, and task type.
- Optional fields such as description and estimated study time can be stored.
- New tasks are created with a valid initial status.
- Invalid task input returns validation errors.
- The created task belongs to the authenticated user.

Development Tasks:

- Design Task entity.
- Create Task repository.
- Implement task request and response DTOs.
- Implement `POST /tasks`.
- Add validation for required fields and allowed values.
- Test task creation with valid and invalid input.

### US-006: View Task List

As a student, I want to view all my tasks, so that I can understand my current academic workload.

Acceptance Criteria:

- The user can retrieve a list of their own tasks.
- The task list includes title, task type, deadline, priority, status, and completion time if available.
- Tasks from other users are not returned.
- The API returns an empty list when the user has no tasks.

Development Tasks:

- Implement `GET /tasks`.
- Filter tasks by authenticated user.
- Create task list response DTO.
- Add optional sorting by deadline or creation time.
- Test task list retrieval for users with and without tasks.

### US-007: View Task Detail

As a student, I want to view the details of a task, so that I can review its deadline, priority, and progress information.

Acceptance Criteria:

- The user can retrieve a task by ID.
- The response includes all task detail fields.
- The user cannot retrieve another user's task.
- A missing task returns a not found response.

Development Tasks:

- Implement `GET /tasks/{id}`.
- Add task ownership validation.
- Add not found handling.
- Test task detail retrieval, not found, and forbidden cases.

### US-008: Update Task Details

As a student, I want to update task details, so that my task list stays accurate when deadlines or study plans change.

Acceptance Criteria:

- The user can update title, description, task type, deadline, priority, and estimated study time.
- The user cannot update another user's task.
- Invalid updates return validation errors.
- Updated task information is persisted.
- Important changes such as deadline or priority updates are recorded as behaviour logs.

Development Tasks:

- Implement `PUT /tasks/{id}`.
- Add update request DTO.
- Add validation for editable fields.
- Add ownership checks.
- Record behaviour logs for significant field changes.
- Test successful update, invalid update, and forbidden update cases.

### US-009: Update Task Status

As a student, I want to update task status, so that I can track whether a task is pending, in progress, or completed.

Acceptance Criteria:

- The user can change task status.
- Allowed statuses are clearly defined.
- When a task is marked as completed, the completion time is recorded.
- If a completed task is reopened, the completion time is handled consistently.
- Status changes are recorded as behaviour logs.

Development Tasks:

- Define task status enum.
- Implement status update logic.
- Record completion timestamp.
- Record status change behaviour logs.
- Test status transitions and completion timestamp behaviour.

### US-010: Delete Task

As a student, I want to delete tasks that are no longer needed, so that my task list remains clean and relevant.

Acceptance Criteria:

- The user can delete their own task.
- The user cannot delete another user's task.
- Deleting a missing task returns a not found response.
- The delete action is recorded as a behaviour log.

Development Tasks:

- Implement `DELETE /tasks/{id}`.
- Add ownership checks.
- Add not found handling.
- Record task deletion behaviour.
- Test successful delete, missing task, and forbidden delete cases.

## Epic 3: Behaviour Tracking and Analytics

### US-011: Record Task Behaviour

As a system, I want to record important task interactions, so that user behaviour can support future analytics and recommendations.

Acceptance Criteria:

- Task creation is recorded.
- Task detail updates are recorded when important fields change.
- Deadline changes are recorded.
- Priority changes are recorded.
- Status changes and task completion are recorded.
- Task deletion is recorded.
- Each behaviour log includes user ID, task ID, action type, and timestamp.

Development Tasks:

- Design BehaviorLog entity.
- Define action type enum.
- Create BehaviorLog repository.
- Implement BehaviorLogService.
- Integrate logging with task operations.
- Test log creation for create, update, complete, and delete actions.

### US-012: View Basic Analytics Summary

As a student, I want to view a basic summary of my study task behaviour, so that I can understand my productivity patterns.

Acceptance Criteria:

- The user can view total task count.
- The user can view completed task count.
- The user can view overdue task count.
- The user can view basic completion rate.
- The summary only uses the authenticated user's data.

Development Tasks:

- Implement analytics service methods.
- Implement `GET /analytics/summary`.
- Calculate task count, completed count, overdue count, and completion rate.
- Add response DTO.
- Test analytics results with sample task data.

## Epic 4: Intelligent Recommendation

### US-013: Extract Recommendation Features

As a system, I want to extract features from task, profile, and behaviour data, so that recommendations are based on measurable signals rather than static sorting.

Acceptance Criteria:

- The system can calculate deadline urgency from task deadline.
- The system can calculate historical completion rate when behaviour data exists.
- The system can use task type or category as a recommendation feature.
- The system can use estimated workload when available.
- The system can use preferred study time or time-of-day data when available.
- Missing historical data is handled with a documented default value.

Development Tasks:

- Implement FeatureExtractionService.
- Define feature DTO or internal feature model.
- Add urgency feature calculation.
- Add historical completion rate calculation.
- Add task type behaviour calculation.
- Add default values for new users with limited data.
- Add unit tests for feature extraction.

### US-014: Calculate Task Priority Score and Delay Risk

As a system, I want to calculate a priority score and delay risk for each active task, so that tasks can be ranked according to urgency, importance, and completion likelihood.

Acceptance Criteria:

- Each active task receives a numeric priority score.
- Deadline urgency affects the score.
- User-defined priority affects the score.
- Estimated workload can affect the score if available.
- Historical completion or delay behaviour can affect the score when data exists.
- Each task receives a delay risk result, such as low, medium, or high.
- The scoring logic is explainable and documented.

Development Tasks:

- Define MVP scoring formula.
- Implement RecommendationService.
- Consume features from FeatureExtractionService.
- Add weighted priority score calculation.
- Add deterministic delay risk prediction.
- Add unit tests for scoring logic.

### US-015: View Recommended Task Order

As a student, I want to view my tasks ranked by priority score, so that I know which task should be completed first.

Acceptance Criteria:

- The user can request recommended task ordering.
- Only the authenticated user's active tasks are included.
- Tasks are sorted by priority score in descending order.
- Each recommended task includes score and basic task information.
- Each recommended task includes delay risk.
- Each recommended task includes a short reason explaining the recommendation.
- Completed or deleted tasks are excluded from active recommendations.

Development Tasks:

- Implement `GET /tasks/recommendations`.
- Retrieve active tasks for authenticated user.
- Apply scoring formula to each task.
- Sort tasks by score.
- Add recommendation response DTO with score, delay risk, and reason.
- Test recommendation ordering and filtering.

## MVP Definition of Done

The MVP is complete when:

- All user management stories from US-001 to US-004 are implemented and tested.
- All task management stories from US-005 to US-010 are implemented and tested.
- Behaviour tracking in US-011 is implemented for core task actions.
- Basic analytics in US-012 is available.
- Feature extraction, priority scoring, delay risk, and recommendation ordering from US-013 to US-015 are implemented.
- The main API behaviour is documented and can be tested through Postman or Swagger.

## Post-MVP User Story Candidates

These stories are intentionally excluded from the MVP and can be added after the core system is complete:

- Generate a personalised daily study schedule.
- Build a dashboard with charts and visual analytics.
- Integrate a Python machine learning service.
- Predict task completion likelihood using a trained model.
- Add calendar integration.
- Deploy the system to a cloud platform.
