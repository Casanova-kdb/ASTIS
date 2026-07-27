# Future Iteration Backlog

## Purpose

This document records the planned post-MVP backlog for ASTIS.

The MVP has already proved the core workflow: users can manage study tasks, the system can track behaviour, and recommendations can be generated from task data. The next iterations should improve intelligence, flexibility, frontend quality, and deployment readiness.

## Post-MVP Roadmap

| Sprint | Theme | Main Outcome |
| --- | --- | --- |
| Sprint 6 | User Story Iteration | Refine backlog and acceptance criteria for post-MVP features |
| Sprint 7 | User Profile and Task Scoring Criteria | Let users manage study profile settings and task-specific scoring criteria |
| Sprint 8 | Account Settings and Profile Management | Let users manage display name and password from Settings |
| Sprint 9 | AI Handbook Parser | Extract task drafts from uploaded module handbook files |
| Sprint 10 | Recommendation Improvement | Improve recommendation explanations and scoring quality |
| Sprint 11 | Redis Recommendation Caching | Cache stable recommendation results and invalidate them after task changes |
| Sprint 12 | Study Plan Generator | Generate a planned study schedule from tasks and recommendations |
| Sprint 13 | Analytics Enhancement | Add richer productivity and delay-pattern analytics |
| Sprint 14 | Frontend Refinement | Polish the main workflows and responsive states |
| Sprint 15 | AI Provider Abstraction | Explore safer provider abstraction without exposing API keys |
| Sprint 16 | Docker and Deployment | Prepare local Docker setup and deployment documentation |

## Sprint 6: User Story Iteration

Goal:

Refine post-MVP features into clearer user stories before implementation.

Expected outputs:

- Updated user story backlog
- New acceptance criteria for advanced features
- Priority labels such as P0, P1, and P2
- Sprint planning notes for the next implementation phase

Example stories:

- As a student, I want to adjust recommendation settings, so that task ranking matches my own study style.
- As a student, I want to upload module information, so that the system can help me create tasks faster.
- As a student, I want a generated study plan, so that I can turn recommendations into an actual schedule.

Output:

The refined user stories are documented in:

```text
docs/user-stories.md
```

## Sprint 7: User Profile and Task Scoring Criteria

Goal:

Allow users to manage personal study settings and configure task-specific scoring criteria.

Possible work:

- User profile settings API
- Task scoring criteria fields
- Five-point task criteria sliders
- Normalised scoring formula
- Frontend Settings page for profile
- Task form criteria controls

Priority:

P1. This improves personalisation while keeping user profile data and task data clearly separated.

## Sprint 8: Account Settings and Profile Management

Goal:

Let users manage their account information and password from the Settings page.

Possible work:

- Current user account API
- Display name update
- Password change with current password verification
- Frontend account settings form
- Frontend password update form
- Backend tests for account update and password change

Expected outcome:

The user can manage basic account details without leaving ASTIS.

Priority:

P1. This improves product completeness and strengthens the user management module.

## Sprint 9: AI Handbook Parser

Goal:

Allow users to upload module handbook files, then extract possible academic tasks.

Possible workflow:

1. User uploads a PDF or DOCX handbook.
2. The backend extracts readable text temporarily.
3. AI or local fallback parsing extracts assignment names, due dates, task types, and possible estimated workload.
4. User reviews and edits the extracted task drafts.
5. User confirms which tasks should be inserted into the task list.

Important design choice:

AI should not directly create final tasks without user review. The user should confirm extracted results first because handbook text can be ambiguous.

Priority:

P1. This is a strong post-MVP AI feature because it connects file processing, AI parsing, and the existing task management workflow.

## Sprint 10: Recommendation Improvement

Goal:

Improve the recommendation module without changing the whole architecture.

Possible work:

- Better explanation text for each recommendation
- More detailed delay risk reasons
- More use of task type and historical behaviour
- Backend tests for more scoring edge cases

Expected outcome:

The user can understand why a task is ranked highly.

Priority:

P1. This improves quality, but the MVP already has a working recommendation module.

## Sprint 11: Redis Recommendation Caching

Goal:

Use Redis where caching has a clear reason.

Good candidate:

- Cache recommendation results for the current user when task data has not changed.

Invalidation rule:

Recommendation cache should be cleared when the user creates, updates, completes, or deletes a task.

Priority:

P2. Redis is useful for engineering demonstration, but it should be added after recommendation behaviour is stable.

## Sprint 12: Study Plan Generator

Goal:

Turn recommended tasks into a practical study schedule.

Possible output:

- Suggested tasks for today
- Study sessions by date
- Estimated time blocks
- Warning when workload is too high before a deadline

Priority:

P1. This makes the product feel more like a study assistant instead of only a ranked task list.

## Sprint 13: Analytics Enhancement

Goal:

Make behaviour logs more visible and useful.

Possible work:

- Weekly completion trend
- Overdue task trend
- Most delayed task type
- Average estimated hours
- Behaviour log list or timeline

Priority:

P2. Useful for presentation and reflection, but less important than core recommendation improvements.

## Sprint 14: Frontend Refinement

Goal:

Improve the MVP frontend so it feels cleaner and easier to demonstrate.

Possible work:

- Improve dashboard visual hierarchy
- Refine task cards and forms
- Improve recommendation cards
- Add clearer empty, loading, success, and error states
- Check responsive layouts

Priority:

P1. This should happen after the next backend feature work is stable, because the UI can then be polished around the final workflow.

## Sprint 15: AI Provider Abstraction

Goal:

Support AI providers in a cleaner and safer way.

Possible work:

- Provider interface in backend code
- DeepSeek provider implementation
- OpenAI-compatible provider option
- Clear fallback behaviour
- Provider-level error handling

User API key idea:

Allowing users to bring their own API key is possible, but it has security risks.

Security concerns:

- API keys must not be exposed to the frontend after submission.
- API keys must not be logged.
- Stored keys should be encrypted if persistence is required.
- The system should avoid accepting arbitrary base URLs without validation.
- If the project is only local, a safer first version is session-only or local environment configuration.

Priority:

P2 for public deployment, because it needs careful security design. It can be explored earlier as a local-only feature.

## Sprint 16: Docker and Deployment

Goal:

Make the project easier to run and review.

Possible work:

- Dockerfile for backend
- Dockerfile for frontend
- Docker Compose for backend, frontend, and MySQL
- Production environment variable documentation
- Deployment notes

Priority:

P2. Good for engineering maturity and portfolio presentation.

## Backlog Principle

Future iterations should not add features randomly.

Each new feature should include:

- User story
- Acceptance criteria
- API or UI impact
- Test plan
- Sprint record

This keeps the project easy to review as a personal Scrum project.
