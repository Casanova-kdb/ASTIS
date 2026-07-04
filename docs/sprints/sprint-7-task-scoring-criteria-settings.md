# Sprint 7: User Profile and Task Scoring Criteria

## Sprint Goal

Add personal study profile settings and task-specific scoring criteria so ASTIS can rank tasks using information that belongs to each task.

This sprint separates three concepts:

```text
User Profile = describes the user
Task Scoring Criteria = describes one task
Recommendation Formula = combines signals into a 0-100 score
```

## Sprint Branch

```text
sprint/7-task-scoring-criteria-settings
```

## User Stories Covered

- US-004 Manage User Profile
- US-018 Configure Task Scoring Criteria

## Scope

Included in this sprint:

- User profile settings
- Default values for user profile
- Task-specific scoring criteria
- Default values for task criteria
- Backend APIs for profile settings
- Task API updates for criteria values
- Frontend Settings page for profile
- Task form sliders for task criteria
- Recommendation formula update

Not included in this sprint:

- AI handbook parser
- Study plan generator
- Redis caching
- Docker deployment
- User-supplied AI provider/API key settings

## Design Decision

The project does not use global recommendation weight sliders for every task.

Instead, task-specific criteria are stored on each task because different academic tasks can have different assessment weight, difficulty, flexibility, and personal importance.

Examples:

```text
Coursework task:
- high grade impact
- high difficulty
- strict deadline
- high personal importance

Reading task:
- low grade impact
- low difficulty
- flexible deadline
- medium personal importance
```

This makes the recommendation model easier to explain because each task carries its own scoring context.

## Backend Work

- Added `user_profiles` table.
- Added task scoring criteria fields:
  - `grade_weight`
  - `difficulty_level`
  - `deadline_flexibility`
  - `personal_importance`
- Added profile default values:
  - `studyPace = NORMAL`
  - `deadlinePressureTolerance = MEDIUM`
  - `dailyStudyCapacity = MEDIUM`
  - `preferredStudyTime = EVENING`
  - `planningStyle = BALANCED`
- Added task criteria default values:
  - `gradeWeight = 3`
  - `difficultyLevel = 3`
  - `deadlineFlexibility = 3`
  - `personalImportance = 3`
- Added profile APIs:
  - `GET /api/settings/profile`
  - `PUT /api/settings/profile`
- Updated task create and update APIs to accept task criteria.
- Updated recommendation feature extraction to include task criteria.

## Scoring Formula Update

Task criteria are converted into normalised 0-1 signals.

The backend then calculates:

```text
score =
  urgencySignal * 0.20
+ userPriorityScore * 0.15
+ workloadScore * 0.10
+ delayRiskScore * 0.10
+ completionRateScore * 0.10
+ gradeWeightScore * 0.15
+ difficultyScore * 0.10
+ deadlineFlexibilityScore * 0.05
+ personalImportanceScore * 0.05
```

The weights add up to 1.0, so the final score stays between 0 and 100.

## Frontend Work

- Added Settings route.
- Added Settings navigation link.
- Added Study Profile form.
- Added task scoring criteria sliders inside the Task form.
- Added task criteria values to task payloads.
- Added criteria summary to task cards.

## Testing and Verification

Backend tests should verify:

- Profile settings can be viewed and updated.
- Task criteria are accepted by task create and update APIs.
- Feature extraction reads task criteria.
- Recommendation score remains normalised between 0 and 100.

Frontend verification should include:

- Settings page loads for authenticated users.
- Profile settings can be saved.
- Task criteria sliders appear in the task form.
- Task criteria are preserved when editing a task.
- Recommendations still load after task criteria changes.

## Definition of Done

Sprint 7 is complete when:

- Users can open the Settings page.
- Users can view and update study profile values.
- Users can configure criteria for each task.
- Recommendation scoring uses task-specific criteria.
- Scores remain within the 0-100 range.
- User profile data and task data remain separated.
- Sprint documentation is updated.
