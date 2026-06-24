# Sprint 1: Project Structure Initialization

## Sprint Goal

Create the initial full-stack project structure for ASTIS using Spring Boot, Vue 3, and MySQL-oriented configuration.

This sprint focuses on establishing a maintainable engineering foundation before implementing business features. It also collects the first visual design assets, including the MVP frontend prototype and the initial database ERD.

## Sprint Branch

```text
sprint/1-project-structure
```

## Planned Outputs

- Spring Boot backend skeleton under `backend/`
- Vue 3 frontend skeleton under `frontend/`
- Root `.gitignore`
- Backend package structure for user, task, analytics, and recommendation modules
- Initial AI recommendation service placeholders
- Initial frontend routes for dashboard, tasks, and recommendations
- MVP frontend prototype under `docs/diagrams/`
- Database ERD under `docs/diagrams/`

## Repository Structure

```text
ASTIS/
  backend/
    src/main/java/com/astis/
      common/
      config/
      health/
      user/
      task/
      analytics/
      recommendation/
    src/main/resources/
    src/test/
  frontend/
    src/
      router/
      services/
      styles/
      views/
  docs/
    diagrams/
    sprints/
```

## Design Assets

- `docs/diagrams/astis-mvp-prototype.pdf`
  - Visily prototype covering the MVP user flow: login/register, dashboard, task management, and AI recommendations.
- `docs/diagrams/astis-database-erd.png`
  - Database design covering users, tasks, behaviour logs, and optional recommendation results.

## Structure Rationale

The backend uses a feature-based modular structure. Each major business area has its own package, such as `user`, `task`, `analytics`, and `recommendation`.

This makes the codebase easier to understand and extend because related controllers, DTOs, entities, repositories, and services stay close to the business module they belong to.

The frontend uses a simple Vue structure with separate folders for routes, API services, styles, and views. This is enough for the MVP while keeping the UI easy to expand in later sprints.

## Definition of Done

- Backend project contains Maven configuration and Spring Boot entry point.
- Backend package structure reflects the planned architecture.
- Frontend project contains Vue 3 and Vite configuration.
- Frontend route structure reflects the planned MVP screens.
- The repository clearly separates backend, frontend, and documentation concerns.
- MVP frontend prototype is stored in the repository.
- Database ERD is stored in the repository.
