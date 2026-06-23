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

## API Design

The system will expose RESTful APIs for authentication, task management, analytics, and recommendations.

Planned API documentation:

- Swagger
- OpenAPI

## Database

The project will use a relational database.

Chosen database:

- MySQL

The initial design will use normalized tables for users, tasks, and behaviour logs.

## Authentication

Authentication will be implemented using:

- Spring Security
- JWT
- Password hashing

Users should only be able to access their own tasks and behaviour records.

## AI Recommendation

The MVP recommendation module will use a Java-based scoring model inside the Spring Boot backend.

Initial scoring factors:

- Deadline urgency
- User-defined priority
- Historical completion rate
- Delay risk

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
