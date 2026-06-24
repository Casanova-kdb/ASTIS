# ASTIS Backend

Spring Boot backend for ASTIS.

## Planned Responsibilities

- User authentication and profile management
- Task CRUD and status tracking
- Behaviour log collection
- Analytics summary APIs
- Embedded AI recommendation module

## Initial Package Structure

```text
com.astis
  common
  config
  health
  user
  task
  analytics
  recommendation
```

## Local Development

The backend is prepared as a Maven-based Spring Boot project.

```bash
cd backend
./mvnw spring-boot:run
```

Maven wrapper files will be added when the backend implementation begins.

## Database Schema

The MySQL schema script is stored at:

```text
src/main/resources/db/schema.sql
```

Run it from the project root with:

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

## Authentication APIs

The first backend authentication endpoints are:

```text
POST /api/auth/register
POST /api/auth/login
```

Both endpoints return a bearer token that can be used as:

```text
Authorization: Bearer <accessToken>
```
