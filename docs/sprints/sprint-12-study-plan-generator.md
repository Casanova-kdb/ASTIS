# Sprint 12: Study Plan Generator

## Goal

Turn the existing ranked task recommendations into a practical seven-day study
schedule. The first version will use deterministic Java planning rules so that
the result is explainable, testable, and available without an AI provider.

## Related User Story

US-022: As a student, I want ASTIS to turn my recommended tasks into a study
plan, so that I know what to work on across the next few days.

Priority: P1

Estimate: 8 story points

Size: L

## Problem

The recommendation page tells the user which task should be considered first,
but it does not answer when the task should be studied or whether the available
time is enough. The study plan generator should connect ranking, estimated
effort, deadlines, and the user's study profile into a schedule that can be
followed.

## Sprint Structure

Sprint 12 is split into five reviewable increments on one sprint branch:

| Increment | Output | Planned Commit |
| --- | --- | --- |
| 12.1 | Planning rules and API contract | `docs: define Sprint 12 study plan design` |
| 12.2 | Deterministic scheduling engine and unit tests | `feat: implement study plan scheduling engine` |
| 12.3 | Authenticated REST API and integration tests | `feat: expose study plan generation API` |
| 12.4 | Vue study plan page and API integration | `feat: add study plan frontend view` |
| 12.5 | Edge-case testing and project documentation | `test: complete study plan validation and documentation` |

Optional AI-generated wording is outside the required path and will only be
considered after the deterministic plan is complete.

## Scope

- Generate a plan for the authenticated user's active tasks.
- Support a configurable planning window from 1 to 14 days.
- Use the existing recommendation rank, priority score, delay risk, deadline,
  and estimated hours.
- Use the user's daily study capacity and preferred study time.
- Split large tasks into study sessions of no more than two hours.
- Keep each day's scheduled work within the user's daily capacity.
- Report work that cannot be scheduled instead of silently dropping it.
- Warn when a deadline has passed or available capacity is insufficient.
- Present the generated plan in a dedicated frontend view.

## Out of Scope

- Persisting generated plans in the database.
- Drag-and-drop calendar editing.
- Calendar provider integration.
- User-defined availability for individual weekdays.
- AI deciding the schedule.
- Redis caching of generated plans.

These features can be considered after the first planning workflow has been
tested with real task data.

## API Contract

### Request

```http
GET /api/study-plans?days=7
Authorization: Bearer <jwt>
```

The `days` query parameter is optional. It defaults to `7` and must be between
`1` and `14`.

### Response

```json
{
  "generatedAt": "2026-07-28T10:30:00",
  "startDate": "2026-07-28",
  "endDate": "2026-08-03",
  "planningDays": 7,
  "preferredStudyTime": "EVENING",
  "dailyCapacityHours": 3.0,
  "totalAvailableHours": 21.0,
  "totalScheduledHours": 8.5,
  "totalUnscheduledHours": 0.0,
  "overloaded": false,
  "warnings": [],
  "days": [
    {
      "date": "2026-07-28",
      "totalScheduledHours": 3.0,
      "sessions": [
        {
          "taskId": 5,
          "title": "Finish database design report",
          "startTime": "18:00",
          "endTime": "20:00",
          "durationHours": 2.0,
          "deadline": "2026-08-06T21:00:00",
          "priorityScore": 72.5,
          "delayRisk": "LOW"
        }
      ]
    }
  ],
  "unscheduledTasks": []
}
```

An unscheduled task entry will contain the task ID, title, remaining hours, and
a machine-readable reason such as `INSUFFICIENT_CAPACITY`,
`DEADLINE_PASSED`, or `OUTSIDE_PLANNING_WINDOW`.

## Planning Rules

### Task Selection

- Include tasks whose status is not `COMPLETED`.
- Reuse the order produced by the recommendation module.
- Use `1.0` hour when estimated hours are missing or zero, and return a warning
  explaining that a default estimate was used.
- Do not silently schedule an overdue task. Return it as unscheduled with a
  `DEADLINE_PASSED` warning.

### Daily Capacity

| Profile Value | Available Study Time |
| --- | --- |
| `LIGHT` | 1.5 hours per day |
| `MEDIUM` | 3.0 hours per day |
| `HEAVY` | 5.0 hours per day |

If no user profile exists, the generator uses the existing default profile:
`MEDIUM` capacity and `EVENING` study time.

### Preferred Start Time

| Profile Value | First Session Start |
| --- | --- |
| `MORNING` | 09:00 |
| `AFTERNOON` | 14:00 |
| `EVENING` | 18:00 |
| `NIGHT` | 20:00 |
| `FLEXIBLE` | 10:00 |

Sessions are separated by a 30-minute break. The frontend will display the
generated blocks but will not allow editing during this sprint.

The listed start time is the preferred target. If a `HEAVY` day would otherwise
finish after 23:30, the generator may move the first session earlier while
keeping the same study period preference.

### Session Allocation

1. Read the current recommendation list and profile.
2. Process active tasks in recommendation order.
3. Split each task into sessions of at most `2.0` hours.
4. Allocate sessions from the first planning day forward.
5. Do not exceed the daily capacity.
6. Do not allocate a session after its task deadline.
7. Record any remaining effort as unscheduled.

The first version deliberately uses a greedy algorithm. It is easier to explain
and test than an AI-generated schedule, while still reusing the intelligent
ranking already produced by ASTIS.

## Warning Rules

The response sets `overloaded` to `true` when at least one active task has
remaining hours that cannot be scheduled before its deadline within the
planning window.

Warnings should distinguish between:

- A deadline that has already passed.
- Insufficient daily capacity before a deadline.
- Work that falls outside the selected planning window.
- A task that used the default one-hour estimate.

Warnings are part of the API response and do not prevent the rest of the plan
from being generated.

## Planned Backend Structure

```text
studyplan/
├── controller/
│   └── StudyPlanController.java
├── dto/
│   ├── DailyStudyPlanResponse.java
│   ├── StudyPlanResponse.java
│   ├── StudyPlanWarningResponse.java
│   ├── StudySessionResponse.java
│   └── UnscheduledTaskResponse.java
├── model/
│   ├── DailyCapacityPolicy.java
│   ├── PlannedSession.java
│   └── UnscheduledReason.java
└── service/
    ├── StudyPlanGeneratorService.java
    └── StudyPlanQueryService.java
```

## Planned Frontend Structure

```text
frontend/src/
├── services/
│   └── studyPlanService.js
└── views/
    └── StudyPlanView.vue
```

The view will provide loading, empty, error, overloaded, and populated states.
It will also provide a control for selecting the planning window and
regenerating the plan.

## Acceptance Criteria Mapping

| US-022 Acceptance Criterion | Planned Evidence |
| --- | --- |
| The user can request a study plan from active tasks. | Authenticated REST endpoint and frontend page |
| The plan uses deadlines, scores, estimated hours, and delay risk. | Generator input and unit tests |
| The plan groups work into days or study sessions. | Daily and session response DTOs |
| The plan warns when workload is too high before a deadline. | Unscheduled output and overload tests |
| The user can regenerate the plan after task changes. | Non-persisted request-time generation |

## Test Plan

- Empty active-task list returns an empty plan.
- Completed tasks are excluded.
- A large task is split into multiple sessions.
- No day exceeds the configured capacity.
- An urgent task is allocated before a lower-ranked task.
- A session is not placed after its deadline.
- Overdue work is returned as unscheduled.
- Insufficient capacity produces an overload warning.
- Missing estimated hours use the documented default.
- `LIGHT`, `MEDIUM`, and `HEAVY` profiles produce different capacities.
- Requests outside the 1-to-14-day range are rejected.
- One user cannot generate a plan from another user's tasks.

## Definition of Done

- All five increments are committed separately.
- US-022 acceptance criteria are covered by implementation or automated tests.
- The endpoint is visible and testable in Swagger.
- The Vue page handles all expected UI states.
- Backend tests and the frontend production build pass in CI.
- README, user stories, Sprint 12 results, and changelog are updated.
- The sprint branch is merged into `develop` through a pull request.
