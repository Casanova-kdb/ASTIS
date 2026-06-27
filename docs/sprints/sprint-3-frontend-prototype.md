# Sprint 3: Frontend Prototype and MVP UI

## Sprint Goal

Build a Vue frontend MVP prototype for ASTIS and connect it with the backend APIs completed in Sprint 2.

The main goal of this sprint is to make the system usable from a browser, not only from Swagger. By the end of this sprint, a user should be able to register, log in, manage study tasks, view dashboard data, and see recommendation results with AI study advice.

## Sprint Branch

```text
sprint/3-frontend-prototype
```

## User Stories Covered

- US-001 Register Account
- US-002 Login Account
- US-006 Create Task
- US-007 View Task List
- US-008 Update Task Details
- US-009 Update Task Status
- US-010 Delete Task
- US-014 Basic Analytics Summary
- US-017 Recommended Task Ordering
- US-018 AI Study Advice

## Implemented Features

- Added frontend API service layer for auth, tasks, analytics, and recommendations.
- Added login and register pages.
- Added JWT-based frontend authentication state using local storage.
- Added route guards for protected pages.
- Added logout action in the sidebar.
- Built task management UI with create, read, update, status update, delete, and status filter.
- Built dashboard UI with task summary metrics, completion rate, and top recommended task.
- Built recommendation UI with ranked tasks, priority score, delay risk, recommendation reason, and AI study advice.
- Added local CORS support so the Vue dev server can call the Spring Boot backend.
- Ignored local demo seed data so testing data is not committed to the repository.

## API Integration

The frontend connects to the following backend endpoints:

```text
POST /api/auth/register
POST /api/auth/login
GET /api/tasks
POST /api/tasks
PUT /api/tasks/{taskId}
PATCH /api/tasks/{taskId}/status
DELETE /api/tasks/{taskId}
GET /api/analytics/summary
GET /api/recommendations/tasks
GET /api/recommendations/advice
```

## Manual Testing

The following flow was tested locally:

- Register a new user.
- Log in with JWT authentication.
- Confirm protected routes redirect unauthenticated users to the login page.
- Create a new task.
- Edit task details.
- Update task status from pending to in progress or completed.
- Filter tasks by status.
- Delete a task.
- View dashboard summary data.
- View the top recommended task on the dashboard.
- View ranked recommendations.
- View AI study advice.

## Test Data

Local sample data was used to test the frontend pages with realistic task records.

The seed file is kept local and ignored by Git because it is only for local demonstration and manual testing.

## Known Limitations

- The UI is still an MVP prototype and can be polished further.
- There are no automated frontend tests yet.
- Dashboard analytics are still basic.
- AI advice depends on the backend DeepSeek configuration.
- Sample data is local only and not part of the committed repository.

## Definition of Done

Sprint 3 is complete when:

- The frontend can run locally with Vite.
- Users can register, log in, and log out.
- Protected pages cannot be accessed without login.
- Task CRUD can be completed from the frontend.
- Dashboard data is displayed from backend APIs.
- Recommendation ranking is displayed from backend APIs.
- AI study advice is displayed from backend APIs.
- The main MVP user flow can be tested manually from the browser.

## Next Sprint

Sprint 4 will focus on full-stack integration polish, manual testing records, UI improvements, and documentation updates before preparing the project for final review.
