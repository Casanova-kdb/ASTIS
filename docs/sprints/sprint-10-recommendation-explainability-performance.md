# Sprint 10: Recommendation Explainability and Performance

## Goal

Improve the recommendation workflow without changing its scoring formula. The sprint makes each ranking easier for a student to understand and removes repeated database statistics queries from the recommendation list request.

## Related User Story

US-017: As a student, I want to understand why a task is recommended, so that I can make an informed study decision.

## Scope

- Return a readable list of ranking factors and a separate delay-risk explanation for every recommendation.
- Keep the existing `reason` field for Dashboard and AI-advice compatibility.
- Query user task totals, completed totals, and overdue totals once per recommendation request.
- Reuse the aggregated statistics while calculating features for every active task.
- Show the factors and risk note on the Vue Recommendations page.
- Add automated coverage for the explanation response and the single aggregation query.

## Query Design

Before this sprint, each task in a recommendation list called feature extraction independently. Each call read the task and repeated aggregate count queries for the same user.

After this sprint, the recommendation service:

1. Loads the authenticated user and active tasks.
2. Runs one aggregate query for total, completed, and overdue task counts.
3. Reuses those statistics and one reference time for every task feature calculation.
4. Sorts the unchanged scores and returns explanation data with the ranked list.

This reduces repeated user-statistics queries as the number of tasks grows. Redis is intentionally deferred until this database-level optimisation has been measured in a stable recommendation workflow.

## API Output

Each recommended task now includes:

- `reason`: short compatibility summary for existing consumers.
- `explanationFactors`: readable factors behind the ranking.
- `delayRiskReason`: a separate explanation of the delay-risk level.

## Acceptance Criteria

- A ranked task returns at least one explanation factor and a non-empty delay-risk reason.
- The recommendation page presents the factors and risk note in a readable layout.
- A multi-task recommendation request calls `findUserTaskStatistics` once for the user.
- Recommendation order and scoring formula remain unchanged.
- Backend tests and frontend production build pass.

## Definition of Done

- Recommendation query optimisation is implemented and covered by an integration test.
- Existing Dashboard and AI advice continue to use the compatibility `reason` field.
- Sprint documentation explains the query trade-off and why Redis is not added yet.
- Changes are merged through CI into `develop`.
