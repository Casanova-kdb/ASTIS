# ASTIS

ASTIS is an AI-enhanced study task management system for academic task planning, behaviour tracking, task prioritisation, and personalised study advice.

The project is built as a completed full-stack MVP with a Spring Boot backend, MySQL database, Vue frontend, and an AI recommendation/advice module.

## Current Status

ASTIS v1.2.0 is the current full-stack release. It adds explainable recommendations, Redis-backed recommendation caching, and deterministic study-plan generation to the completed v1.1.0 engineering foundation.

Completed areas:

- Backend authentication and task APIs
- Task behaviour logging
- Basic analytics summary
- Priority scoring and delay risk calculation
- Recommended task ordering
- DeepSeek-based AI study advice with local fallback
- Vue frontend prototype
- Manual full-stack test report
- MVP release summary
- Post-MVP iteration backlog
- Account settings and password management
- Task-specific scoring criteria and study profile settings
- AI handbook parser for PDF/DOCX task draft extraction
- Redis-backed recommendation caching with task-change invalidation
- Deterministic study plan generation from recommendations, deadlines, estimated effort, and study preferences
- Enhanced analytics with weekly completion and overdue trends, delayed task types, and workload estimates
- GitHub Actions CI and protected `develop` / `main` branches

## MVP Features

- User registration and login with JWT authentication
- Protected frontend routes
- Task create, read, update, status update, and delete
- Task filtering by status
- Behaviour log recording for key task actions
- Dashboard summary for task progress
- Recommendation ranking based on task data
- AI study advice based on recommended tasks
- AI-assisted handbook import with user-confirmed task creation
- Swagger/OpenAPI backend documentation

## Tech Stack

| Area | Tools |
| --- | --- |
| Backend | Java, Spring Boot, Spring Web, Spring Security, Spring Data JPA |
| Frontend | Vue 3, Vite, Vue Router, Axios, Chart.js |
| Data | MySQL, Redis recommendation cache |
| AI | DeepSeek API, local fallback advice, handbook parsing fallback |
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
REDIS_ENABLED
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
REDIS_CONNECT_TIMEOUT
REDIS_COMMAND_TIMEOUT
REDIS_RECOMMENDATION_TTL
```

For local development, use a long JWT secret, for example:

```text
JWT_SECRET=astis-local-development-jwt-secret-that-is-long-enough-for-hmac-sha384-2026
```

If `DEEPSEEK_API_KEY` is not configured, ASTIS returns local fallback study advice.

Recommendation results are cached in Redis at `localhost:6379` for 10 minutes by
default. The cache is cleared after task changes, and recommendation requests
continue with database calculation if Redis is unavailable.

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
| Users | `GET /api/users/me`, `PUT /api/users/me`, `PUT /api/users/me/password` |
| Tasks | `GET /api/tasks`, `POST /api/tasks`, `PUT /api/tasks/{taskId}` |
| Analytics | `GET /api/analytics/summary`, `GET /api/analytics/trends?weeks=8` |
| Recommendations | `GET /api/recommendations/tasks` |
| AI Advice | `GET /api/recommendations/advice` |
| Handbooks | `POST /api/handbooks/parse` |
| Study Plans | `GET /api/study-plans?days=7` |

## Documentation

- [Requirements Definition](docs/01-requirements-definition.md)
- [System Design](docs/02-system-design.md)
- [Technical Stack](docs/03-technical-stack.md)
- [User Stories](docs/user-stories.md)
- [Database Design](docs/database-design.md)
- [MVP Release Summary](docs/mvp-release-summary.md)
- [v1.1.0 Release Notes](docs/releases/v1.1.0.md)
- [v1.2.0 Release Notes](docs/releases/v1.2.0.md)
- [Changelog](CHANGELOG.md)
- [Future Iteration Backlog](docs/future-iteration-backlog.md)
- [Manual Full-stack Test Report](docs/testing/manual-test-report.md)

Sprint records:

- [Sprint 0: Project Planning and Setup](docs/sprints/sprint-0-planning.md)
- [Sprint 1: Project Structure Initialization](docs/sprints/sprint-1-project-structure.md)
- [Sprint 2: Backend API and Recommendation MVP](docs/sprints/sprint-2-backend-api-swagger.md)
- [Sprint 3: Frontend Prototype and MVP UI](docs/sprints/sprint-3-frontend-prototype.md)
- [Sprint 4: Full-stack Integration Polish](docs/sprints/sprint-4-integration-polish.md)
- [Sprint 5: MVP Release Closure](docs/sprints/sprint-5-mvp-release-closure.md)
- [Sprint 6: User Story Iteration](docs/sprints/sprint-6-user-story-iteration.md)
- [Sprint 7: User Profile and Task Scoring Criteria](docs/sprints/sprint-7-task-scoring-criteria-settings.md)
- [Sprint 8: Account Settings and Profile Management](docs/sprints/sprint-8-account-settings-profile-management.md)
- [Sprint 9: AI Handbook Parser](docs/sprints/sprint-9-ai-handbook-parser.md)
- [Sprint 10: Recommendation Explainability and Performance](docs/sprints/sprint-10-recommendation-explainability-performance.md)
- [Sprint 11: Redis Recommendation Caching](docs/sprints/sprint-11-redis-recommendation-caching.md)
- [Sprint 12: Study Plan Generator](docs/sprints/sprint-12-study-plan-generator.md)
- [Sprint 13: Analytics Enhancement](docs/sprints/sprint-13-analytics-enhancement.md)
- [Sprint 14: Frontend Refinement](docs/sprints/sprint-14-frontend-refinement.md)

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
- Pull requests to protected branches must pass backend tests and a frontend production build.
- Sprint records and testing notes are kept under `docs/`.

## Notes

Local demo seed data is ignored by Git and is not part of the repository.

The current UI is an MVP prototype with study planning and enhanced analytics. Later work can improve visual polish, add automated frontend tests, and prepare Docker-based deployment.
