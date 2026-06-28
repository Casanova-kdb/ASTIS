# ASTIS MVP Release Summary

## Release Information

| Item | Value |
| --- | --- |
| Product name | ASTIS |
| MVP version | v1.0 |
| Release status | Completed |
| Release type | Local full-stack MVP |
| Release branch | `sprint/5-mvp-release-closure` |

## Purpose

This document records the first complete MVP release of ASTIS.

The goal of this release is to show that the project has moved from planning and prototype design into a working full-stack system with authentication, task management, analytics, recommendation ranking, and AI-supported study advice.

## MVP Scope

The MVP focuses on the core student workflow:

1. A student creates an account and logs in.
2. The student creates and manages academic tasks.
3. The backend records important task actions as behaviour logs.
4. The system calculates basic analytics from task data.
5. The recommendation module ranks active tasks by priority score and delay risk.
6. The frontend displays dashboard metrics, task management, recommendations, and AI study advice.

## Completed Features

### User and Security

- User registration
- User login
- Password hashing
- JWT authentication
- Protected backend APIs
- Protected frontend routes
- Frontend logout and expired-token handling

### Task Management

- Create academic tasks
- View task list
- Edit task details
- Update task status
- Delete tasks
- Filter tasks by status
- Task ownership protection

### Behaviour Tracking and Analytics

- Behaviour logs for task creation, update, status update, completion, and deletion
- Basic dashboard summary
- Total task count
- Completed task count
- Pending task count
- Overdue task count
- Completion rate

### Recommendation and AI

- Recommendation feature extraction
- Deadline urgency calculation
- Task priority weight
- Estimated workload support
- User completion rate
- Overdue task ratio
- Priority scoring
- Delay risk classification
- Recommended task ordering
- DeepSeek-based AI study advice
- Local fallback advice when an API key is not configured

### Frontend MVP

- Vue login page
- Vue register page
- Dashboard page
- Task management page
- Recommendation page
- Frontend API service layer
- Basic error handling
- Task action success feedback

## Architecture Summary

ASTIS uses a modular full-stack structure:

```text
Vue Frontend
    |
REST API
    |
Spring Boot Backend
    |
Controller Layer
    |
Service Layer
    |
Repository Layer
    |
MySQL Database
```

The backend is organised by feature modules instead of placing all entities, services, and controllers into one flat folder. This makes the project easier to explain, extend, and review.

## AI Integration Summary

The MVP does not only send a prompt to an external AI model.

The backend first calculates structured recommendation data using task records and user behaviour signals. After that, the AI advice feature uses the ranked task results to generate study advice in natural language.

This means the AI part has two layers:

- Explainable recommendation logic built inside the backend
- Optional language-model advice through DeepSeek

This design keeps the core recommendation result stable even when an external AI API is unavailable.

## Testing Summary

Manual full-stack testing has been completed locally.

Result:

```text
Total test cases: 15
Passed: 15
Failed: 0
Partial: 0
Blocked: 0
```

The test report is stored in:

```text
docs/testing/manual-test-report.md
```

Main tested flows:

- Register and login
- Protected route redirect
- Logout
- Task create, read, update, status update, filter, and delete
- Dashboard summary
- Recommendation ranking
- AI study advice
- Backend unavailable error handling

## Known Limitations

- The UI is still MVP-level and can be improved visually.
- Automated frontend tests have not been added yet.
- Deployment is not included in the MVP release.
- Recommendation scoring is rule-based and has not been trained on a large dataset.
- User-configurable recommendation weights are not included yet.
- User-configurable AI provider/API key support is not included yet.
- Module handbook parsing is not included yet.
- Redis caching is not needed yet because recommendation traffic is local and small.

## MVP Definition of Done

The MVP is considered complete because:

- Core user authentication works.
- Core task management works from the frontend and backend.
- Task actions are recorded as behaviour logs.
- Basic analytics are available in the dashboard.
- Recommendation scoring and delay risk are available.
- AI study advice is available with fallback behaviour.
- Main user stories are traceable through documentation.
- Full-stack manual testing has been recorded.
- The project has a documented post-MVP backlog.

## Next Step

After this MVP release, the project should move into post-MVP iteration planning.

The next sprint should focus on refining user stories and deciding which advanced features should be implemented first.
