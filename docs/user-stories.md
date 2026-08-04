# User Stories

## Purpose

This document defines the MVP and post-MVP user stories for ASTIS.

The goal is to make sure each feature can be traced from user need to acceptance criteria and development tasks. These stories can be converted into GitHub Issues and implemented through sprint branches.

## Estimation Guide

Each story includes priority, estimate, and size to support sprint planning.

- Priority: P0 means required for the MVP, P1 means important but can be finished after the core flow, and P2 means useful but not blocking.
- Estimate: Story points based on relative effort.
- Size: S, M, or L based on expected implementation complexity.

## GitHub Project Board Backlog

This table can be used as the starting backlog for a GitHub Projects Kanban board. Each user story can become one GitHub Issue, and the fields can be added as issue labels or project custom fields.

| ID | Issue Title | Epic | Priority | Estimate | Size | Planned Sprint | Kanban Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-001 | User Registration | User Management and Security | P0 | 3 | M | Sprint 2 | Done |
| US-002 | User Login | User Management and Security | P0 | 3 | M | Sprint 2 | Done |
| US-003 | Protected Personal Workspace | User Management and Security | P0 | 5 | M | Sprint 2 | Done |
| US-004 | Manage User Profile | User Management and Security | P1 | 5 | M | Sprint 8 | Done |
| US-005 | Create Academic Task | Task Management | P0 | 3 | M | Sprint 2 | Done |
| US-006 | View Task List | Task Management | P0 | 2 | S | Sprint 2 | Done |
| US-007 | View Task Detail | Task Management | P0 | 2 | S | Sprint 2 | Done |
| US-008 | Update Task Details | Task Management | P0 | 5 | M | Sprint 2 | Done |
| US-009 | Update Task Status | Task Management | P0 | 3 | M | Sprint 2 | Done |
| US-010 | Delete Task | Task Management | P0 | 2 | S | Sprint 2 | Done |
| US-011 | Record Task Behaviour | Behaviour Tracking and Analytics | P0 | 5 | M | Sprint 2 | Done |
| US-012 | View Basic Analytics Summary | Behaviour Tracking and Analytics | P1 | 5 | M | Sprint 2 | Done |
| US-013 | Extract Recommendation Features | Intelligent Recommendation | P0 | 5 | M | Sprint 2 | Done |
| US-014 | Calculate Task Priority Score and Delay Risk | Intelligent Recommendation | P0 | 8 | L | Sprint 2 | Done |
| US-015 | View Recommended Task Order | Intelligent Recommendation | P0 | 5 | M | Sprint 2 | Done |
| US-016 | View AI Study Advice | Intelligent Recommendation | P1 | 3 | M | Sprint 2 | Done |
| US-017 | Improve Recommendation Explanation | Intelligent Recommendation | P1 | 5 | M | Sprint 10 | Done |
| US-018 | Configure Task Scoring Criteria | Task Recommendation | P1 | 8 | L | Sprint 7 | Done |
| US-019 | View Enhanced Analytics | Analytics Enhancement | P2 | 5 | M | Sprint 13 | Done |
| US-020 | Upload Module Handbook | AI Handbook Parser | P1 | 5 | M | Sprint 9 | Done |
| US-021 | Review AI-extracted Tasks | AI Handbook Parser | P1 | 8 | L | Sprint 9 | Done |
| US-022 | Generate Study Plan | Study Planning | P1 | 8 | L | Sprint 12 | Done |
| US-023 | Configure AI Provider Safely | AI Provider Abstraction | P2 | 8 | L | Sprint 15 | Backlog |
| US-024 | Improve Frontend Presentation | Frontend Refinement | P1 | 5 | M | Sprint 14 | Backlog |
| US-025 | Cache Recommendation Results | Performance | P2 | 5 | M | Sprint 11 | Done |
| US-026 | Run Project with Docker | Deployment | P2 | 5 | M | Sprint 16 | Backlog |
| US-027 | Update Deployment Documentation | Deployment | P2 | 3 | S | Sprint 16 | Backlog |

Suggested GitHub labels:

- `priority: P0`, `priority: P1`, `priority: P2`
- `size: S`, `size: M`, `size: L`
- `type: user-story`
- `epic: user-management`, `epic: task-management`, `epic: analytics`, `epic: recommendation`, `epic: ai-parser`, `epic: deployment`
- `sprint-2`, `sprint-4`, `sprint-5`, `sprint-6`, `sprint-7`, `sprint-8`, `sprint-9`, `sprint-10`, `sprint-11`, `sprint-12`, `sprint-13`, `sprint-14`

## MVP Coverage Map

| MVP Capability | Covered By |
| --- | --- |
| User registration and login | US-001, US-002 |
| Authenticated access control | US-003 |
| Task creation | US-005 |
| Task list and task detail retrieval | US-006, US-007 |
| Task update and status tracking | US-008, US-009 |
| Task deletion | US-010 |
| Behaviour logging | US-011 |
| Basic analytics summary | US-012 |
| AI feature extraction | US-013 |
| AI-based task scoring and delay risk | US-014 |
| Recommended task ordering | US-015 |
| AI study advice | US-016 |

## Post-MVP Coverage Map

| Post-MVP Capability | Covered By |
| --- | --- |
| User profile and study preference editing | US-004 |
| Better recommendation explanations | US-017 |
| Task-specific scoring criteria | US-018 |
| Richer analytics and behaviour insight | US-019 |
| Module handbook upload or paste input | US-020 |
| AI extraction review before task creation | US-021 |
| Generated study plan | US-022 |
| Configurable AI provider/API key design | US-023 |
| Frontend visual and usability refinement | US-024 |
| Recommendation caching | US-025 |
| Docker and deployment preparation | US-026, US-027 |

## Epic 1: User Management and Security

### US-001: User Registration

Priority: P0
Estimate: 3 story points
Size: M

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

Priority: P0
Estimate: 3 story points
Size: M

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

Priority: P0
Estimate: 5 story points
Size: M

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

Priority: P1
Estimate: 3 story points
Size: M

As a student, I want to manage my study profile, so that the system can personalise recommendations based on my habits and preferences.

Acceptance Criteria:

- The user can view their profile.
- The user can update basic profile fields such as display name.
- The user can store optional study preference data, such as preferred study time.
- Profile data is only accessible to the authenticated user.
- The user can change their password after providing the current password.
- Wrong current password returns a clear error.
- New password is stored as a hash, not plain text.
- Profile data can be used by the recommendation module when available.

Development Tasks:

- Extend User entity or create UserProfile fields.
- Implement profile request and response DTOs.
- Implement `GET /users/me`.
- Implement `PUT /users/me`.
- Implement `PUT /users/me/password`.
- Add ownership protection through authenticated identity.
- Test profile retrieval and update cases.
- Test password update with correct and incorrect current password.

## Epic 2: Task Management

### US-005: Create Academic Task

Priority: P0
Estimate: 3 story points
Size: M

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

Priority: P0
Estimate: 2 story points
Size: S

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

Priority: P0
Estimate: 2 story points
Size: S

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

Priority: P0
Estimate: 5 story points
Size: M

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

Priority: P0
Estimate: 3 story points
Size: M

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

Priority: P0
Estimate: 2 story points
Size: S

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

Priority: P0
Estimate: 5 story points
Size: M

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

Priority: P1
Estimate: 5 story points
Size: M

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

Priority: P0
Estimate: 5 story points
Size: M

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

Priority: P0
Estimate: 8 story points
Size: L

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

Priority: P0
Estimate: 5 story points
Size: M

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

### US-016: View AI Study Advice

Priority: P1
Estimate: 3 story points
Size: M

As a student, I want to receive short AI-generated study advice based on my recommended tasks, so that I can understand how to start my study session.

Acceptance Criteria:

- The user can request AI study advice from the recommendation page.
- The advice is based on the current user's recommended task list.
- The system does not expose another user's task data to the AI request.
- If the external AI provider is unavailable or not configured, the system returns local fallback advice.
- The frontend displays the advice clearly without blocking the recommendation list.

Development Tasks:

- Implement AI advice service.
- Build a prompt from ranked recommendation results.
- Add external AI provider client.
- Add local fallback advice.
- Add API endpoint for recommendation advice.
- Display advice in the frontend recommendation page.

## MVP Definition of Done

The MVP v1.0 is complete when:

- Core user management stories from US-001 to US-003 are implemented and tested.
- All task management stories from US-005 to US-010 are implemented and tested.
- Behaviour tracking in US-011 is implemented for core task actions.
- Basic analytics in US-012 is available.
- Feature extraction, priority scoring, delay risk, and recommendation ordering from US-013 to US-015 are implemented.
- AI study advice in US-016 is available with fallback behaviour.
- The main API behaviour is documented and can be tested through Postman or Swagger.

US-004 profile management is kept as a P1 post-MVP improvement because the first release can already provide the main authenticated task and recommendation workflow without a profile settings page.

## Post-MVP User Stories

These stories are intentionally excluded from the MVP and can be added after the core system is complete.

### US-017: Improve Recommendation Explanation

Priority: P1
Estimate: 5 story points
Size: M

As a student, I want each recommendation to explain why a task is ranked highly, so that I can trust the system instead of seeing it as a black box.

Acceptance Criteria:

- Each recommendation includes a short reason.
- The reason mentions the strongest signals, such as deadline urgency, priority, workload, or delay risk.
- The explanation is deterministic and can be tested without calling an external AI API.
- The frontend displays the reason beside each ranked task.
- Existing recommendation ranking still works after explanation changes.

Development Tasks:

- Refine recommendation reason generation.
- Add tests for common explanation cases.
- Update frontend recommendation card copy.
- Check that explanations match the calculated feature values.

### US-018: Configure Task Scoring Criteria

Priority: P1
Estimate: 8 story points
Size: L

As a student, I want each task to have its own scoring criteria, so that ASTIS can rank different types of academic work more accurately.

Acceptance Criteria:

- Each task can store task-specific scoring criteria.
- New tasks receive default scoring criteria values.
- The user can adjust criteria such as grade impact, difficulty, deadline flexibility, and personal importance.
- Criteria values are validated within a safe range.
- Recommendation scoring uses each task's own criteria.
- User profile data remains separate from task scoring criteria.
- Recommendation score remains normalised between 0 and 100.

Development Tasks:

- Add task scoring criteria fields to the Task entity.
- Add request and response DTOs.
- Update task create and update APIs.
- Update feature extraction to include task criteria.
- Update scoring service to use task-specific criteria.
- Add frontend controls inside the task form.
- Add backend tests for scoring and criteria validation.

### US-019: View Enhanced Analytics

Priority: P2
Estimate: 5 story points
Size: M

As a student, I want to see more detailed study analytics, so that I can understand my delay patterns and productivity trends.

Acceptance Criteria:

- The user can view weekly task completion trend.
- The user can view overdue task trend.
- The user can see which task type is most often delayed.
- Analytics only use the authenticated user's data.
- The frontend displays the data in a readable dashboard layout.

Development Tasks:

- Extend analytics service queries.
- Add analytics response DTOs.
- Add endpoint for trend analytics.
- Add frontend dashboard sections or charts.
- Add service tests for analytics calculations.

Implementation Evidence:

- `GET /api/analytics/trends` returns a configurable 4-to-12-week reporting window.
- Repository queries are bounded by the authenticated user and reporting dates.
- Weekly completion and overdue trends include zero-value weeks.
- The response includes the most delayed task type and average estimated hours.
- The Dashboard displays completion and overdue charts with 4, 8, and 12-week controls.
- Backend unit, repository, and controller tests cover calculation rules, validation, and user isolation.

### US-020: Upload Module Handbook

Priority: P1
Estimate: 5 story points
Size: M

As a student, I want to upload a module handbook file, so that ASTIS can help me find possible assessment tasks and deadlines.

Acceptance Criteria:

- The user can upload a PDF or DOCX module handbook.
- The backend extracts readable text from the uploaded file.
- The system validates empty or unsupported input.
- Uploaded content is only processed temporarily.
- The raw handbook file is not permanently stored in the MVP version.
- The raw content is not exposed to other users through the API.

Development Tasks:

- Add PDF and DOCX text extraction support.
- Add backend endpoint for handbook file parsing.
- Add validation for file size, empty files, and unsupported extensions.
- Add frontend form for uploading handbook files.
- Add error handling for unsupported or empty content.

### US-021: Review AI-extracted Tasks

Priority: P1
Estimate: 8 story points
Size: L

As a student, I want to review AI-extracted tasks before they are added to my task list, so that incorrect deadlines are not saved automatically.

Acceptance Criteria:

- The AI parser returns extracted task candidates with title, deadline, task type, and confidence or explanation.
- Extracted tasks are shown as draft items.
- The user can edit extracted task details before saving.
- Fields that cannot be confidently extracted are left empty for the user to complete.
- Late-submission or extension information can be used as task deadline flexibility when it is clearly present.
- The user can create each confirmed draft through the existing task creation flow.
- The system does not automatically create final tasks without user confirmation.
- If the parser cannot find a clear deadline, the UI requires the user to add one before creating a task.
- Failed AI parsing returns a clear error or fallback message.

Development Tasks:

- Design extracted task candidate DTO.
- Build AI parsing prompt and response format.
- Add parser service with provider fallback handling.
- Add review UI for extracted draft tasks.
- Connect confirmed draft tasks to existing task creation flow.
- Add tests for successful parse, invalid AI response, and user-confirmed creation.

### US-022: Generate Study Plan

Priority: P1
Estimate: 8 story points
Size: L

As a student, I want ASTIS to turn my recommended tasks into a study plan, so that I know what to work on across the next few days.

Acceptance Criteria:

- The user can request a study plan from active tasks.
- The plan uses task deadlines, priority scores, estimated hours, and delay risk.
- The plan groups work into days or study sessions.
- The plan warns the user when estimated workload is too high before a deadline.
- The user can regenerate the plan after task changes.

Development Tasks:

- Design study plan response DTO.
- Implement planning logic based on recommendations and estimated hours.
- Add optional AI wording for plan explanation.
- Add frontend study plan view.
- Add tests for short deadline, high workload, and empty task list cases.

### US-023: Configure AI Provider Safely

Priority: P2
Estimate: 8 story points
Size: L

As a student or project owner, I want AI provider settings to be configurable safely, so that the system is not locked to one provider and does not expose API keys.

Acceptance Criteria:

- The backend has an AI provider abstraction instead of provider-specific logic scattered across services.
- DeepSeek remains supported as one provider.
- The system can support OpenAI-compatible providers in a controlled way.
- API keys are never returned to the frontend after submission.
- API keys are never written to application logs.
- If API keys are stored, they are encrypted or clearly marked as local-only for development.
- Invalid provider configuration returns a safe error message.

Development Tasks:

- Create AI provider interface.
- Move DeepSeek-specific logic behind the provider interface.
- Add provider configuration validation.
- Decide whether user API keys are stored, session-only, or environment-only.
- Add tests for missing key, invalid provider, and fallback behaviour.
- Document security limitations clearly.

### US-024: Improve Frontend Presentation

Priority: P1
Estimate: 5 story points
Size: M

As a student, I want the application interface to feel clearer and more polished, so that the system is easier to use and demonstrate.

Acceptance Criteria:

- Main pages have consistent spacing, typography, and button states.
- Dashboard information is easy to scan.
- Task forms remain readable on different screen widths.
- Recommendation cards do not feel visually crowded.
- Empty, loading, success, and error states are shown consistently.
- The app still works with the existing backend APIs.

Development Tasks:

- Review current frontend layout.
- Refine dashboard, task page, and recommendation page.
- Improve empty and loading states.
- Check responsive layouts.
- Run frontend build after UI changes.

### US-025: Cache Recommendation Results

Priority: P2
Estimate: 5 story points
Size: M

As a system, I want to cache recommendation results when task data has not changed, so that repeated recommendation requests are faster.

Acceptance Criteria:

- Recommendation results can be cached per authenticated user.
- Cache entries are invalidated when the user creates, updates, completes, or deletes a task.
- Cache does not leak data between users.
- If Redis is unavailable, the system can still generate recommendations directly.
- The caching behaviour is documented.

Development Tasks:

- Add Redis dependency and configuration.
- Add recommendation cache key design.
- Cache recommendation results after calculation.
- Invalidate cache from task mutation operations.
- Add tests or manual verification for cache hit and invalidation behaviour.
- Update setup documentation.

### US-026: Run Project with Docker

Priority: P2
Estimate: 5 story points
Size: M

As a reviewer, I want to run the project with Docker, so that I can start the system without manually configuring every service.

Acceptance Criteria:

- Docker Compose can start backend, frontend, and MySQL locally.
- Environment variables are documented.
- The backend can connect to the database container.
- The frontend can call the backend API.
- Local secrets are not committed to Git.

Development Tasks:

- Add backend Dockerfile.
- Add frontend Dockerfile.
- Add Docker Compose file.
- Add example environment file.
- Test local container startup.

### US-027: Update Deployment Documentation

Priority: P2
Estimate: 3 story points
Size: S

As a reviewer, I want clear setup and deployment notes, so that I can understand how ASTIS can be run outside the developer's machine.

Acceptance Criteria:

- README explains local setup and Docker setup.
- Required environment variables are listed.
- AI provider configuration is explained safely.
- Database setup is documented.
- Known deployment limitations are listed.

Development Tasks:

- Update README deployment section.
- Add Docker setup steps.
- Add environment variable table.
- Document local-only limitations.
- Add troubleshooting notes for common startup issues.
