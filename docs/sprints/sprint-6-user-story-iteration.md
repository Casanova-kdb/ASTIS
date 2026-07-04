# Sprint 6: User Story Iteration

## Sprint Goal

Refine the ASTIS post-MVP backlog into clear user stories before starting new feature implementation.

This sprint does not add product code. It focuses on making the next development phase easier to plan, review, and explain.

## Sprint Branch

```text
sprint/6-user-story-iteration
```

## Why This Sprint Exists

After the MVP release, the project has several possible directions:

- Better recommendation logic
- Task-specific scoring criteria
- Richer analytics
- Module handbook parsing
- Study plan generation
- AI provider abstraction
- Frontend refinement
- Redis caching
- Docker and deployment

Instead of implementing these randomly, this sprint turns them into user stories with priorities, estimates, acceptance criteria, and planned sprint numbers.

## Completed Work

- Updated the user story document from MVP-only coverage to MVP plus post-MVP coverage.
- Added a post-MVP coverage map.
- Added missing AI study advice story as a completed MVP story.
- Added post-MVP user stories from US-017 to US-027.
- Added priorities using P0, P1, and P2.
- Added estimates and sizes for GitHub Projects planning.
- Added acceptance criteria and development tasks for each future story.
- Connected future work to the planned sprint roadmap.

## New Post-MVP Stories

| ID | Story | Planned Sprint |
| --- | --- | --- |
| US-017 | Improve Recommendation Explanation | Sprint 7 |
| US-018 | Customise Recommendation Weights | Sprint 8 |
| US-019 | View Enhanced Analytics | Sprint 9 |
| US-020 | Upload or Paste Module Handbook | Sprint 10 |
| US-021 | Review AI-extracted Tasks | Sprint 10 |
| US-022 | Generate Study Plan | Sprint 11 |
| US-023 | Configure AI Provider Safely | Sprint 12 |
| US-024 | Improve Frontend Presentation | Sprint 12 |
| US-025 | Cache Recommendation Results | Sprint 13 |
| US-026 | Run Project with Docker | Sprint 14 |
| US-027 | Update Deployment Documentation | Sprint 14 |

## Important Design Decisions

### AI Handbook Parser

The AI handbook parser should not directly create final tasks.

It should first return draft task candidates. The user should review, edit, and confirm them before they are saved. This keeps the workflow safer because AI extraction can be wrong when module handbook text is unclear.

### User-supplied AI API Key

User-supplied AI keys are useful, but they create security risks.

For the first version, provider configuration should be handled carefully. If API keys are ever accepted from users, the backend must not return them to the frontend, log them, or store them without a clear security plan.

### Task Scoring Criteria

Task-specific scoring criteria should be implemented before Redis caching.

The scoring logic should be stable before the system starts caching recommendation results.

## Definition of Done

Sprint 6 is complete when:

- Post-MVP features are represented as user stories.
- Each new story has acceptance criteria.
- Each new story has priority, estimate, and size.
- The backlog can be copied into GitHub Projects as Issues.
- The next implementation sprint has a clear starting point.

## Next Sprint

Sprint 7 should begin implementation again.

The recommended next feature is recommendation improvement, because it strengthens the core AI/product value before adding larger features such as custom weights or handbook parsing.
