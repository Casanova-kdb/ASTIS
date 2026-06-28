# Sprint 2: Backend API and Recommendation MVP

## Sprint Goal

Build the main backend MVP for ASTIS using Spring Boot.

This sprint focuses on turning the initial project structure into a working backend system with authentication, task management, behaviour tracking, analytics, recommendation scoring, AI study advice, and Swagger API documentation.

By the end of this sprint, ASTIS should be usable as a backend API platform before the frontend MVP is built in Sprint 3.

## Sprint Branch

```text
sprint/2-backend-api-swagger
```

## User Stories Covered

- US-001 Register Account
- US-002 Login Account
- US-006 Create Task
- US-007 View Task List
- US-008 Update Task Details
- US-009 Update Task Status
- US-010 Delete Task
- US-011 Record Task Behaviour
- US-014 Basic Analytics Summary
- US-015 Extract Recommendation Features
- US-016 Priority Scoring and Delay Risk
- US-017 Recommended Task Ordering
- US-018 AI Study Advice

## Implemented Backend Modules

### User Module

- User registration
- User login
- Password hashing with BCrypt
- JWT token generation
- JWT authentication filter
- Security configuration for protected APIs

### Task Module

- Create task
- View task list
- View task detail
- Update task details
- Update task status
- Delete task
- Task ownership validation by logged-in user

### Analytics Module

- Behaviour log entity and repository
- Behaviour logging service
- Logs for important task actions:
  - task creation
  - task update
  - deadline update
  - priority update
  - status update
  - task completion
  - task deletion
- Basic analytics summary endpoint

### Recommendation Module

- Feature extraction from task data
- Deadline proximity calculation
- Urgency score calculation
- Priority weight calculation
- Estimated hours support
- User completion rate calculation
- Overdue task ratio calculation
- Priority score calculation
- Delay risk classification
- Recommended task ordering endpoint

### AI Module

- DeepSeek API client
- AI study advice service
- Prompt generation based on recommended tasks
- Local fallback advice when the AI API key is not configured

## API Endpoints

Main endpoints completed in this sprint:

```text
POST /api/auth/register
POST /api/auth/login

POST /api/tasks
GET /api/tasks
GET /api/tasks/{taskId}
PUT /api/tasks/{taskId}
PATCH /api/tasks/{taskId}/status
DELETE /api/tasks/{taskId}

GET /api/analytics/summary

GET /api/recommendations/tasks
GET /api/recommendations/advice
```

## Database Work

The backend uses MySQL with JPA entities and repositories.

Core tables:

```text
users
tasks
behavior_logs
recommendation_results
```

The executable schema is stored at:

```text
backend/src/main/resources/db/schema.sql
```

## Swagger and API Documentation

Swagger/OpenAPI support was added so backend APIs can be tested and reviewed through the browser.

Local Swagger URL:

```text
http://localhost:8080/api/swagger-ui/index.html
```

Swagger was used during development to manually test authentication, task APIs, analytics, recommendations, and AI advice endpoints.

## AI Integration Design

The recommendation logic is not only a direct prompt to an external AI model.

The backend first calculates structured recommendation data using its own scoring logic:

```text
task data
deadline proximity
task priority
estimated hours
completion history
overdue ratio
```

Then the AI advice module uses the ranked recommendation results to generate natural language study advice.

This keeps the system data-driven while still using AI to improve user-facing study planning guidance.

## Testing and Verification

Backend verification included:

- Spring Boot integration tests
- Service-level recommendation tests
- Authentication controller tests
- Task controller tests
- Analytics controller tests
- Recommendation controller tests
- Local Swagger API testing
- Local DeepSeek/fallback AI advice testing

Main test command:

```bash
mvn -q -f backend/pom.xml test
```

## Key Engineering Decisions

- Used Spring Security with JWT for stateless authentication.
- Used JPA repositories for faster MVP development and cleaner entity persistence.
- Kept backend packages feature-based instead of putting all entities/controllers/services into one flat layer.
- Recorded behaviour logs automatically from task actions instead of requiring a separate frontend workflow.
- Built the recommendation model as an extensible scoring service instead of hard-coding a static task order.
- Added AI advice after recommendation scoring, so AI explains and extends the system output rather than replacing the core logic.

## Known Limitations

- Recommendation scoring is still a rule-based MVP model.
- Behaviour logs are recorded but not yet visualised in the frontend.
- AI provider configuration is backend-level, not user-configurable yet.
- No advanced machine learning model is included at this stage.
- Deployment is not completed in this sprint.

## Definition of Done

Sprint 2 is complete when:

- Users can register and log in through backend APIs.
- JWT-protected endpoints can identify the current user.
- Users can create, view, update, complete, and delete tasks.
- Task actions are recorded as behaviour logs.
- Analytics summary data can be retrieved.
- Recommendation features can be extracted from task data.
- Task priority score and delay risk can be calculated.
- Recommended task ordering can be returned by API.
- AI study advice can be generated or fallback advice can be returned.
- Swagger API documentation is available.
- Backend tests pass locally.

## Next Sprint

Sprint 3 will focus on building the Vue frontend MVP prototype and connecting it to the backend APIs completed in this sprint.
