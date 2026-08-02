# Sprint 13: Analytics Enhancement

Status: Analytics calculations implemented, API and frontend work pending

## Goal

Turn existing task and behaviour data into readable productivity trends so a
student can understand completion patterns, missed deadlines, and the type of
work that is most often delayed.

## Related User Story

US-019: As a student, I want to see more detailed study analytics, so that I
can understand my delay patterns and productivity trends.

Priority: P2

Estimate: 5 story points

Size: M

## Problem

The current Dashboard only shows totals and a completion rate. These values
describe the current task list, but they do not show whether productivity is
improving, when missed deadlines occurred, or which task type is repeatedly
delayed.

Sprint 13 will add a bounded trend query and a small set of clearly defined
metrics. It will reuse existing task and behaviour data without introducing a
separate analytics database.

## Sprint Structure

Sprint 13 is split into five reviewable increments on one sprint branch:

| Increment | Output | Planned Commit |
| --- | --- | --- |
| 13.1 | Metric definitions, API contract, and test plan | `docs: define Sprint 13 analytics contract` |
| 13.2 | Trend queries, calculations, DTOs, and service tests | `feat: add analytics trend calculations` |
| 13.3 | Authenticated REST endpoint and integration tests | `feat: expose enhanced analytics API` |
| 13.4 | Dashboard charts and frontend API integration | `feat: add dashboard analytics visualizations` |
| 13.5 | Edge-case validation and project documentation | `test: complete Sprint 13 validation and documentation` |

## Scope

- Return a weekly completion trend.
- Return a weekly overdue trend.
- Identify the task type with the most delayed work.
- Return the average estimated hours for tasks with a usable estimate.
- Support a configurable reporting window from 4 to 12 weeks.
- Include zero-value weeks so the frontend receives a stable chart shape.
- Restrict every calculation to the authenticated user.
- Present the results in the existing Dashboard.
- Preserve the current `/analytics/summary` response for compatibility.

## Out of Scope

- Persisting daily or weekly analytics snapshots.
- Comparing one user with another user.
- Predictive analytics or AI-written analytics summaries.
- Arbitrary date-range selection.
- CSV or spreadsheet export.
- A complete behaviour-log timeline.
- Redis caching for analytics responses.

The behaviour timeline and export ideas remain useful backlog candidates, but
they are not required by US-019.

## Metric Definitions

### Reporting Window

- A week starts on Monday and ends on Sunday.
- The current partial week is included.
- `weeks=8` means the current week plus the previous seven calendar weeks.
- Dates use the backend application's configured local time zone.
- The API returns ISO-8601 dates so the frontend does not need to infer week
  boundaries.

### Weekly Completion Count

The completion trend counts recorded `COMPLETE_TASK` behaviour events whose
`created_at` falls inside each reporting week.

This uses behaviour data rather than only the current task status, so a later
task reopen does not remove the historical completion event. A task that is
completed more than once after being reopened can produce more than one
completion event; the chart therefore represents completion activity.

### Weekly Overdue Count

A task contributes to the overdue trend when its deadline falls inside a
reporting week and either:

- it is not completed at the report generation time; or
- its `completed_at` value is later than its deadline.

The task is grouped by the week containing its deadline. An on-time completed
task is not overdue. A task whose deadline is still in the future is not
overdue.

Deleted tasks are not included in overdue calculations because their task row
no longer exists. This limitation will be documented rather than hidden.

### Most Delayed Task Type

The most delayed task type is calculated from the same overdue-task population
inside the selected reporting window. Task types are compared by delayed task
count.

If two task types have the same count, use alphabetical task-type order as the
stable tie-breaker. If there are no delayed tasks, return `null`.

### Average Estimated Hours

Average estimated hours uses the authenticated user's currently stored tasks
with a non-null estimate greater than zero. Missing and zero estimates are
excluded. Return `0.0` when no usable estimate exists.

The value is a supporting metric and does not change task scoring or study-plan
generation.

## API Contract

### Request

```http
GET /api/analytics/trends?weeks=8
Authorization: Bearer <jwt>
```

The `weeks` query parameter is optional. It defaults to `8` and must be between
`4` and `12`, inclusive.

### Successful Response

The existing `ApiResponse` envelope is retained:

```json
{
  "success": true,
  "message": "Analytics trends retrieved",
  "data": {
    "generatedAt": "2026-08-01T14:30:00",
    "startDate": "2026-06-08",
    "endDate": "2026-08-02",
    "weeks": 8,
    "weeklyTrends": [
      {
        "weekStart": "2026-07-27",
        "weekEnd": "2026-08-02",
        "completedCount": 4,
        "overdueCount": 1
      }
    ],
    "mostDelayedTaskType": {
      "taskType": "COURSEWORK",
      "delayedCount": 3
    },
    "averageEstimatedHours": 2.5
  }
}
```

`weeklyTrends` always contains exactly the requested number of entries in
ascending week order. `mostDelayedTaskType` is `null` when no delayed task
exists in the reporting window.

### Invalid Window Response

```json
{
  "success": false,
  "message": "Analytics weeks must be between 4 and 12",
  "data": null
}
```

The endpoint returns HTTP `400 Bad Request` for an out-of-range or non-numeric
value.

## Data Sources

| Metric | Source | Relevant Fields |
| --- | --- | --- |
| Weekly completion | `behavior_logs` | `user_id`, `action_type`, `created_at` |
| Weekly overdue | `tasks` | `user_id`, `deadline`, `status`, `completed_at` |
| Most delayed type | `tasks` | `user_id`, `task_type`, `deadline`, `completed_at` |
| Average estimated hours | `tasks` | `user_id`, `estimated_hours` |

No metric will parse the free-text `old_value` or `new_value` columns. Core
analytics must use structured columns and enum values.

## Query and Calculation Design

1. Resolve the authenticated user by email.
2. Calculate the requested Monday-to-Sunday reporting buckets once.
3. Load matching completion events with one bounded repository query.
4. Load tasks relevant to the selected deadline window with one bounded query.
5. Calculate the average estimate with one aggregate query.
6. Group the bounded results in the service layer.
7. Fill missing weeks with zero counts.
8. Return immutable response DTOs in ascending week order.

Grouping weeks in Java avoids database-specific date functions and keeps the
same behaviour in MySQL and H2 integration tests. The design avoids one query
per week and does not load another user's rows.

The implementation should add composite indexes that match the bounded query
patterns:

```text
behavior_logs (user_id, action_type, created_at)
tasks (user_id, deadline, completed_at)
```

## Planned Backend Structure

```text
analytics/
├── controller/
│   └── AnalyticsController.java
├── dto/
│   ├── AnalyticsSummaryResponse.java
│   ├── AnalyticsTrendResponse.java
│   ├── DelayedTaskTypeResponse.java
│   └── WeeklyAnalyticsResponse.java
├── repository/
│   └── BehaviorLogRepository.java
└── service/
    ├── AnalyticsService.java
    └── AnalyticsTrendService.java
```

`AnalyticsService` will continue to own the existing summary workflow.
`AnalyticsTrendService` will own the new bounded trend calculation so the
existing class does not become responsible for unrelated response shapes.

## Planned Frontend Work

```text
frontend/src/
├── services/
│   └── analyticsService.js
└── views/
    └── DashboardView.vue
```

The Dashboard will keep its existing summary metrics and recommended focus.
Sprint 13 will add:

- a 4, 8, or 12-week reporting-window control;
- a completion trend chart;
- an overdue trend chart;
- a most-delayed-task-type summary;
- average estimated hours;
- loading, empty, and error states for the trend request.

The visual design will remain restrained because the complete cross-page UI
refinement belongs to Sprint 14.

## Acceptance Criteria Mapping

| US-019 Acceptance Criterion | Planned Evidence |
| --- | --- |
| The user can view weekly task completion trend. | Completion-event query, weekly DTOs, chart, and tests |
| The user can view overdue task trend. | Deadline-based overdue calculation, chart, and tests |
| The user can see which task type is most often delayed. | Delayed task-type response and deterministic tie-break test |
| Analytics only use the authenticated user's data. | User-scoped queries and cross-user integration test |
| The frontend displays the data in a readable dashboard layout. | Dashboard charts and desktop/mobile browser evidence |

## Test Plan

- The endpoint requires authentication.
- The default request returns eight ordered weeks.
- Valid 4-week and 12-week windows are accepted.
- Values below 4, above 12, and non-numeric values return HTTP 400.
- An empty account receives zero-filled weeks, a null delayed type, and a zero
  average estimate.
- Completion events are grouped into the correct Monday-to-Sunday week.
- Weeks without completion events remain in the response with zero.
- An incomplete task past its deadline is counted as overdue.
- A task completed after its deadline is counted as overdue.
- A task completed before its deadline is not counted as overdue.
- A future deadline is not counted as overdue.
- The most delayed task type and alphabetical tie-break are deterministic.
- Missing and zero estimates are excluded from the average.
- Another user's tasks and behaviour events are excluded.
- The trend endpoint appears in Swagger/OpenAPI.

## Definition of Done

- [x] Metric definitions, reporting window, and edge-case rules are documented.
- [x] The API request and response contract are documented.
- [x] Query boundaries and index requirements are documented.
- [x] US-019 acceptance criteria map to planned implementation evidence.
- [x] Trend calculation and repository queries are implemented.
- [ ] The authenticated endpoint and automated tests are implemented.
- [ ] Dashboard visualizations and expected UI states are implemented.
- [ ] Complete backend tests and the frontend production build pass.
- [ ] README, user stories, backlog, changelog, and Sprint results are updated.
- [ ] The sprint branch is merged into `develop` through a pull request.
