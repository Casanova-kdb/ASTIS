# Changelog

All notable changes to ASTIS are documented in this file.

## [v1.1.0] - 2026-07-24

### Added

- Account settings for updating a display name and changing a password.
- Study profile settings and task-specific scoring criteria.
- AI-assisted PDF and DOCX module handbook import with editable task drafts.
- DeepSeek handbook parsing with a safe local fallback when AI configuration or responses are unavailable.
- GitHub Actions CI for backend tests and frontend production builds.
- A reusable pull request template for sprint, user-story, acceptance, and verification evidence.

### Changed

- Handbook parsing now preserves source dates and returns missing fields for user review instead of inventing values.
- Overdue tasks are handled as lower-priority work unless late submission or resubmission remains possible.
- User integration tests now clear dependent task records before user records, making the test suite stable on clean CI runners.

### Engineering

- `develop` and `main` require pull requests, successful CI checks, and resolved conversations before merging.
- Force pushes and branch deletion are disabled for protected branches.

## [v1.0] - 2026-06

### MVP Release

- JWT authentication, protected routes, and task CRUD.
- Behaviour logging, dashboard analytics, recommendation ranking, delay risk, and AI study advice with fallback behaviour.
- Vue frontend MVP, Swagger/OpenAPI documentation, and manual full-stack test evidence.

[v1.1.0]: https://github.com/Casanova-kdb/ASTIS/releases/tag/v1.1.0
