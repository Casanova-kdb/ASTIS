# Sprint 5: MVP Release Closure

## Sprint Goal

Close the first ASTIS MVP release by documenting what has been completed, confirming the MVP boundary, and preparing a clear post-MVP backlog.

This sprint is mainly a release and documentation sprint. It does not add a major new feature. The purpose is to show that the project has reached a stable MVP point before moving into more advanced iterations.

## Sprint Branch

```text
sprint/5-mvp-release-closure
```

## Why This Sprint Exists

Before starting more advanced features, the MVP needs a clear closing point.

Without this sprint, the project could look like an endless list of features. With this sprint, the project has a visible version boundary:

```text
Planning -> Backend -> Frontend -> Integration Polish -> MVP Release Closure
```

This makes the project easier to explain in a portfolio, application, or interview.

## Scope

Included in this sprint:

- MVP release summary
- MVP completion evidence
- README documentation update
- User story status alignment
- Future iteration backlog
- Clear list of known limitations

Not included in this sprint:

- New backend feature implementation
- New frontend page implementation
- Redis caching
- Docker deployment
- Module handbook parser
- User-configurable AI provider settings

## Completed Work

- Added `docs/mvp-release-summary.md`.
- Added `docs/future-iteration-backlog.md`.
- Added this Sprint 5 record.
- Updated `README.md` to mark the project as MVP v1.0 completed.
- Added README links to the MVP release summary, future backlog, Sprint 2, Sprint 4, and Sprint 5 records.
- Updated user story board status for completed MVP stories.
- Clarified that profile management is a post-MVP story, not a blocker for MVP v1.0.

## MVP Features Confirmed

- User registration and login
- JWT authentication
- Protected personal workspace
- Task CRUD
- Task status updates
- Behaviour logging for task actions
- Basic analytics summary
- Recommendation feature extraction
- Priority scoring and delay risk
- Recommended task ordering
- AI study advice with local fallback
- Vue frontend MVP
- Manual full-stack test report

## Testing Evidence

The MVP was manually tested through the full-stack application.

Test result:

```text
Total test cases: 15
Passed: 15
Failed: 0
Partial: 0
Blocked: 0
```

Detailed report:

```text
docs/testing/manual-test-report.md
```

Because this sprint is documentation-focused, no new backend or frontend test cases were required.

## Known Limitations

- UI design is still MVP-level.
- Automated frontend tests are not added yet.
- Deployment is not completed yet.
- Recommendation scoring is rule-based.
- User-customised recommendation weights are not implemented yet.
- Module handbook upload and AI parsing are not implemented yet.
- AI provider/API key configuration is backend-level, not user-level.
- Redis caching is not implemented yet.

## Definition of Done

Sprint 5 is complete when:

- The MVP release summary exists.
- The README clearly says the MVP is completed.
- The main completed features are listed.
- Manual test results are linked.
- Future iteration backlog is documented.
- Completed user stories are marked consistently.
- The project has a clear next-step plan after MVP.

## Next Sprint

Sprint 6 should focus on user story iteration for post-MVP features.

The main output should be a refined backlog for advanced features such as recommendation improvement, custom scoring weights, handbook parsing, study plan generation, AI provider abstraction, Redis caching, and Docker deployment.
