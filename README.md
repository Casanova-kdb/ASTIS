# ASTIS

ASTIS is an AI-enhanced study task management system for academic task planning, behaviour tracking, task prioritisation, and personalised study advice.

The project is built as a full-stack MVP with a Spring Boot backend, MySQL database, Vue frontend, and an AI recommendation/advice module.

## Current Status

ASTIS is currently at the MVP prototype stage.

Completed areas:

- Backend authentication and task APIs
- Task behaviour logging
- Basic analytics summary
- Priority scoring and delay risk calculation
- Recommended task ordering
- DeepSeek-based AI study advice with local fallback
- Vue frontend prototype
- Manual full-stack test report

## MVP Features

- User registration and login with JWT authentication
- Protected frontend routes
- Task create, read, update, status update, and delete
- Task filtering by status
- Behaviour log recording for key task actions
- Dashboard summary for task progress
- Recommendation ranking based on task data
- AI study advice based on recommended tasks
- Swagger/OpenAPI backend documentation

## Tech Stack

| Area | Tools |
| --- | --- |
| Backend | Java, Spring Boot, Spring Web, Spring Security, Spring Data JPA |
| Frontend | Vue 3, Vite, Vue Router, Axios |
| Database | MySQL |
| AI | DeepSeek API, local fallback advice |
| API Docs | Swagger / OpenAPI |
| Testing | JUnit, Spring Boot Test, manual full-stack testing |
| Workflow | Lightweight personal Scrum, sprint branches, GitHub PRs |

## Project Structure

```text
ASTIS/
  backend/      Spring Boot REST API and recommendation module
  frontend/     Vue 3 frontend MVP prototype
  docs/         Requirements, design, user stories, diagrams, sprints, tests
```

## Backend Setup

Create the MySQL database schema:

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

Run the backend:

```bash
cd backend
mvn spring-boot:run
```

Backend default URL:

```text
http://localhost:8080/api
```

Swagger UI:

```text
http://localhost:8080/api/swagger-ui/index.html
```

## Backend Environment Variables

The backend can run with default local values, but these environment variables can be configured:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MS
DEEPSEEK_ENABLED
DEEPSEEK_BASE_URL
DEEPSEEK_API_KEY
DEEPSEEK_MODEL
DEEPSEEK_TEMPERATURE
DEEPSEEK_MAX_TOKENS
```

For local development, use a long JWT secret, for example:

```text
JWT_SECRET=astis-local-development-jwt-secret-that-is-long-enough-for-hmac-sha384-2026
```

If `DEEPSEEK_API_KEY` is not configured, ASTIS returns local fallback study advice.

## Frontend Setup

Run the frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend default URL:

```text
http://localhost:5173
```

Frontend API base URL is configured in:

```text
frontend/.env.example
```

Default value:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## Main API Modules

| Module | Example Endpoints |
| --- | --- |
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Tasks | `GET /api/tasks`, `POST /api/tasks`, `PUT /api/tasks/{taskId}` |
| Analytics | `GET /api/analytics/summary` |
| Recommendations | `GET /api/recommendations/tasks` |
| AI Advice | `GET /api/recommendations/advice` |

## Documentation

- [Requirements Definition](docs/01-requirements-definition.md)
- [System Design](docs/02-system-design.md)
- [Technical Stack](docs/03-technical-stack.md)
- [User Stories](docs/user-stories.md)
- [Database Design](docs/database-design.md)
- [Manual Full-stack Test Report](docs/testing/manual-test-report.md)

Sprint records:

- [Sprint 0: Project Planning and Setup](docs/sprints/sprint-0-planning.md)
- [Sprint 1: Project Structure Initialization](docs/sprints/sprint-1-project-structure.md)
- [Sprint 3: Frontend Prototype and MVP UI](docs/sprints/sprint-3-frontend-prototype.md)

Design assets:

- [MVP frontend prototype](docs/diagrams/astis-mvp-prototype.pdf)
- [Database ERD](docs/diagrams/astis-database-erd.png)
- [ASTIS MVP Prototype on Visily](https://app.visily.ai/projects/3c280140-c6a8-4b9d-b58b-fa5d75d226a2/boards/2648718/presenter?play-mode=All+screens)

## Manual Testing

Manual full-stack testing was completed locally.

Result:

```text
Total test cases: 15
Passed: 15
Failed: 0
Partial: 0
Blocked: 0
```

Covered flows:

- Register and login
- Protected route redirect
- Task CRUD
- Task status update and filtering
- Dashboard analytics
- Recommendation ranking
- AI study advice
- Backend unavailable error handling

See [Manual Full-stack Test Report](docs/testing/manual-test-report.md) for details.

## Workflow

This project follows a lightweight personal Scrum workflow:

- Requirements and user stories are documented before implementation.
- Each sprint has a focused goal and branch.
- Features are developed through small commits.
- PR descriptions are used to explain sprint outputs.
- Sprint records and testing notes are kept under `docs/`.

## Notes

Local demo seed data is ignored by Git and is not part of the repository.

The current UI is an MVP prototype. Later work can improve visual polish, add automated frontend tests, and prepare cloud deployment.
