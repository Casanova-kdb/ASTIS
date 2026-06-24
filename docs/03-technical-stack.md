# Phase 3: Technical Stack

## Backend

The backend will be implemented with Java and Spring Boot.

Planned backend technologies:

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- Lombok

## Frontend

The frontend will be implemented with Vue 3.

Planned frontend technologies:

- Vue 3
- Vite
- Vue Router
- Axios

Vue is selected because it supports a clear and maintainable UI structure while keeping the project focus on backend architecture, behaviour tracking, and AI-based recommendation.

## API Design

The system will expose RESTful APIs for authentication, task management, analytics, and recommendations.

Planned API documentation:

- Swagger
- OpenAPI

API design standards:

- Consistent endpoint naming
- DTOs for request and response payloads
- Proper HTTP status codes
- Validation errors with clear messages
- Global exception handling

## Database

The project will use a relational database.

Chosen database:

- MySQL

The initial design will use normalized tables for users, tasks, and behaviour logs.

The executable schema script is stored at:

```text
backend/src/main/resources/db/schema.sql
```

Database design standards:

- Normalized schema for core entities
- Indexes on common query fields such as user ID, task status, deadline, and behaviour timestamp
- Clear relationships between users, tasks, and behaviour logs

## Authentication

Authentication will be implemented using:

- Spring Security
- JWT
- Password hashing

Users should only be able to access their own tasks and behaviour records.

## AI Recommendation

The MVP recommendation module will use a Java-based AI scoring model inside the Spring Boot backend. It must be implemented as an embedded service-layer module rather than a text-generation API wrapper.

Initial scoring factors:

- Deadline urgency
- User-defined priority
- Historical completion rate
- Delay risk
- Task type or category behaviour
- Time decay factor
- Estimated workload

The AI module will include:

- Feature extraction layer
- Priority scoring model
- Delay risk prediction
- Recommendation ranking
- Explanation generation for recommendation results

Future versions may introduce:

- Python service
- scikit-learn
- Completion likelihood prediction
- More advanced behaviour analytics

## Testing

Planned testing tools:

- JUnit
- Spring Boot Test
- Postman
- Basic integration tests

Testing will cover core backend behaviour, API responses, validation, and recommendation scoring logic.

## Engineering Standards

The implementation should demonstrate application-level engineering practice:

- Clear separation of controller, service, repository, DTO, entity, and AI service responsibilities
- Consistent request validation
- Global exception handling
- Meaningful Git commits
- Sprint-based branch workflow
- Reproducible local setup instructions

## Engineering Tools

Planned engineering workflow tools:

- Git
- GitHub Repository
- GitHub Issues
- GitHub Projects
- VS Code

Optional tools:

- Docker
- Redis
- Cloud deployment platform

## Documentation

Project documentation will be maintained in Markdown under `docs/`.

Planned documentation includes:

- Requirements definition
- System design
- Technical stack
- User stories
- Sprint planning and review records
- Architecture and ERD diagrams
