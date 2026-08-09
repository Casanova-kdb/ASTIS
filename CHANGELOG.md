# Changelog

All notable changes to ASTIS are documented in this file.

## [Unreleased]

### Added

- Configurable 4, 8, and 12-week completion and overdue trend analytics.
- Most-delayed task type and average estimated-hours analytics.
- Dashboard charts with independent loading, empty, and error states.
- Authenticated `GET /api/analytics/trends` endpoint with stable zero-filled weekly data.
- Responsive desktop and mobile application navigation with accessible icon controls.
- Shared frontend design tokens, page headers, authentication framing, and structural loading states.

### Changed

- Analytics queries are bounded by user and reporting window to avoid repeated per-task statistics queries.
- Dashboard completion rate now displays the stored decimal value as the correct percentage.
- Dashboard, Tasks, Recommendations, Study Plan, Handbook Import, Settings, Login, and Register now use a consistent task-focused visual hierarchy.
- Detailed scoring and recommendation evidence remains available without crowding the primary workflow.

### Testing

- Analytics service, repository, and controller coverage for grouping, overdue rules, validation, empty data, and user isolation.
- Complete backend suite: 83 tests passed locally.
- Frontend production build and dependency audit passed with 0 vulnerabilities.
- Full-stack registration, task creation, authenticated navigation, and recommendation smoke flow passed locally.
- Browser checks passed at 1440 px, 1024 px, and 390 px with no remaining page overflow, unnamed buttons, unlabelled controls, duplicate IDs, or console errors.

## [v1.2.0] - 2026-07-31

### Added

- Readable recommendation factors and a separate delay-risk explanation for every ranked task.
- Redis-backed recommendation caching with a configurable 10-minute TTL.
- Per-user cache invalidation after task creation, update, status change, or deletion.
- Graceful database calculation when Redis operations are unavailable.
- Deterministic study plan generation for configurable 1-to-14-day windows.
- Daily study sessions based on recommendation order, deadlines, estimated effort, user capacity, and preferred study time.
- Explicit warnings and unscheduled-work output for overdue tasks, missing estimates, and insufficient capacity.
- A responsive Vue Study Plan page with regeneration controls and daily schedule output.

### Changed

- Recommendation requests now load user task statistics once and reuse them across all active tasks.
- Recommendation ranking is isolated behind a cacheable service without changing the scoring formula.

### Testing

- Automated coverage for cache hits, task-change invalidation, and recommendation JSON serialization.
- Study Plan unit and API integration coverage for session splitting, capacity limits, deadlines, user isolation, empty workloads, and overload warnings.
- Complete backend suite: 71 tests passed locally.
- Frontend production build and responsive Study Plan browser checks passed locally.

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

[Unreleased]: https://github.com/Casanova-kdb/ASTIS/compare/v1.2.0...HEAD
[v1.2.0]: https://github.com/Casanova-kdb/ASTIS/releases/tag/v1.2.0
[v1.1.0]: https://github.com/Casanova-kdb/ASTIS/releases/tag/v1.1.0
