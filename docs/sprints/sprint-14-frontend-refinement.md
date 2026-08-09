# Sprint 14: Frontend Refinement

Status: Complete

## Goal

Refine the existing Vue frontend into a consistent and practical study
workspace without replacing the current application or changing backend API
behaviour.

## Related User Story

US-024: As a student, I want the application interface to feel clearer and
more polished, so that the system is easier to use and demonstrate.

Priority: P1

Estimate: 5 story points

Size: M

## Sprint Structure

Sprint 14 is split into five reviewable increments on one sprint branch:

| Increment | Output | Planned Commit |
| --- | --- | --- |
| 14.1 | UI audit, design rules, acceptance mapping, and test plan | `docs: define Sprint 14 frontend refinement` |
| 14.2 | Application shell, navigation, design tokens, and shared controls | `refactor: improve frontend shell and design foundation` |
| 14.3 | Dashboard, Tasks, and Recommendations refinement | `feat: refine core productivity workflows` |
| 14.4 | Study Plan, Handbook Import, Settings, and authentication refinement | `feat: refine planning and account experiences` |
| 14.5 | Responsive checks, accessibility review, build validation, and documentation | `test: complete Sprint 14 frontend validation` |

## Baseline Audit

The current frontend is functional and readable, but it still looks like an
early application prototype.

Observed issues:

- Most information is placed in the same white bordered card style, which
  weakens visual hierarchy.
- The fixed sidebar has no icons, compact state, or mobile navigation pattern.
- The Inter font, dark blue sidebar, and teal buttons create a generic starter
  dashboard appearance.
- Prototype labels such as `Planning Prototype` and `AI Import` receive more
  visual attention than the user's current task or decision.
- Loading states mainly use plain text instead of preserving page structure.
- Long task, handbook, and settings forms need clearer grouping and action
  placement.
- Recommendation explanations contain useful data but become crowded when all
  factors use the same visual weight.
- Shared styling is concentrated in one large stylesheet, making page-specific
  changes harder to review.
- Mobile layouts collapse content but do not provide a complete navigation
  experience.

The audit was based on the Vue source, current global styles, and a browser
review of the running Dashboard on 2026-08-07.

## Design Direction

ASTIS should look like a focused academic productivity tool rather than a
marketing page, a generic admin template, or an AI-generated concept.

The interface will use:

- medium information density;
- clear hierarchy for deadlines, risk, priority, and next actions;
- a neutral canvas with restrained teal, amber, red, and green functional
  colours;
- compact page headings and stable content widths;
- square or lightly rounded controls with a maximum main radius of 8px;
- icons for familiar navigation and actions;
- short transitions only where they clarify hover, focus, or navigation state;
- real application copy and existing backend data.

The interface will avoid:

- purple or blue AI-style gradients;
- glassmorphism, glow effects, decorative texture, and floating background
  shapes;
- oversized headings and marketing-style hero sections;
- excessive card grids, pill labels, and decorative badges;
- large empty areas that reduce task-scanning efficiency;
- animation that delays routine actions;
- copying a complete third-party dashboard template.

## Design Foundation

### Typography

- Use one readable interface font family with 400, 500, 600, and 700 weights.
- Use tabular figures for scores, hours, dates, and analytics values.
- Keep page titles compact and reserve large numbers for real metrics.
- Use sentence case for headings and labels.
- Keep letter spacing at `0` throughout the application.

### Colour Roles

The implementation will use named CSS variables instead of repeated colour
literals:

| Role | Purpose |
| --- | --- |
| Canvas | Page background and content separation |
| Surface | Forms, charts, and grouped work areas |
| Border | Quiet structural separation |
| Text and muted text | Primary and supporting information |
| Primary | Main commands and active navigation |
| Success | Completed work and successful actions |
| Warning | Capacity or deadline attention |
| Danger | Overdue work, high delay risk, and destructive actions |

Status colours must always be paired with readable text, not used as the only
signal.

### Layout

- Keep a desktop navigation rail between 224px and 240px wide.
- Constrain main content to a readable maximum width on large screens.
- Use CSS Grid for stable dashboard, form, and analytics layouts.
- Collapse navigation into a controlled mobile menu below the application
  breakpoint.
- Preserve fixed dimensions for charts, metrics, icon buttons, and segmented
  controls so loading and hover states do not shift the layout.

### Shared States

Every data-driven page should support the states that apply to it:

- initial loading;
- background refresh;
- empty data;
- recoverable API error;
- successful save or creation;
- disabled or submitting command;
- keyboard focus.

Loading indicators should preserve the expected content shape. Error messages
should remain close to the action or data that failed.

## Planned Page Work

### Application Shell

- Add navigation icons and clearer active-state treatment.
- Separate account information from the primary navigation.
- Add a mobile navigation control and prevent background overflow.
- Keep logout and account commands predictable and keyboard accessible.

### Dashboard

- Reduce prototype wording and improve the metric hierarchy.
- Make current focus and deadline risk more prominent than supporting copy.
- Keep analytics charts readable without turning every metric into a card.
- Distinguish initial loading from trend refresh failures.

### Tasks

- Keep task creation and task review visible in one workflow.
- Improve form grouping, scoring controls, and action placement.
- Make status, priority, deadline, and editing state easier to scan.
- Preserve all existing task CRUD behaviour.

### Recommendations

- Prioritise task order, score, risk, and reason.
- Reduce repeated borders and competing labels inside each recommendation.
- Keep detailed explanation factors available without crowding the primary
  decision.

### Study Plan and Handbook Import

- Improve the visual relationship between controls, warnings, sessions, and
  unscheduled work.
- Make the upload, parse, review, and create-task sequence easier to follow.
- Keep missing AI fields and fallback results explicit for user review.

### Settings and Authentication

- Separate profile, study preferences, and password actions clearly.
- Keep destructive or sensitive actions visually distinct.
- Align Login and Register with the main product identity without adding a
  landing-page layout.

## Scope

- Refine all existing Vue views and the shared application shell.
- Introduce a small set of reusable layout and state components when they
  remove real duplication.
- Reorganise frontend styles into reviewable files if the existing global file
  blocks safe iteration.
- Add one consistent icon library when required by shared navigation and
  commands.
- Preserve existing routes, services, API request shapes, and authentication
  behaviour.

## Out of Scope

- Replacing Vue or migrating to TypeScript.
- Adopting Vuestic, Tailwind, Element Plus, or another complete UI framework.
- Changing backend endpoints or recommendation logic.
- Adding dark mode, custom themes, or user-selectable colour palettes.
- Adding marketing pages, payment flows, or public account pages.
- Rewriting working views only to match a third-party template structure.

## Acceptance Criteria Mapping

| US-024 Acceptance Criterion | Validation Evidence |
| --- | --- |
| Main pages have consistent spacing, typography, and button states. | Shared tokens and layout components applied across every view |
| Dashboard information is easy to scan. | Metric strip, focus hierarchy, and analytics review passed |
| Task forms remain readable on different screen widths. | Browser checks passed at 1440 px, 1024 px, and 390 px |
| Recommendation cards do not feel visually crowded. | Primary ranking data is separated from collapsible explanation details |
| Empty, loading, success, and error states are shown consistently. | Structural loading states and page-level status checks passed |
| The app still works with the existing backend APIs. | Registration, task creation, recommendation output, build, and 83 backend tests passed |

## Test Plan

- Run the frontend production build after every implementation increment.
- Check Dashboard, Tasks, Recommendations, Study Plan, Handbook Import,
  Settings, Login, and Register at desktop width.
- Check the application shell and main workflows at 1024px and 390px widths.
- Confirm that navigation, forms, segmented controls, and icon buttons can be
  reached with the keyboard.
- Confirm that long task titles, filenames, dates, errors, and recommendation
  explanations do not overlap or overflow.
- Confirm loading, empty, success, error, disabled, and refresh states.
- Confirm task CRUD, recommendation refresh, handbook parsing, settings, and
  study-plan generation still call the existing services.
- Review browser console errors and run the frontend dependency audit.

## Definition of Done

- [x] Current UI and code structure are audited.
- [x] Visual direction and anti-template constraints are documented.
- [x] Sprint increments and US-024 acceptance evidence are defined.
- [x] Shared application shell and design foundation are implemented.
- [x] Core productivity pages are refined.
- [x] Planning, import, account, and authentication pages are refined.
- [x] Desktop, tablet, mobile, accessibility, and build checks pass.
- [x] README, user stories, backlog, changelog, and Sprint results are updated.
- [ ] The sprint branch is merged into `develop` through a pull request.

## Sprint Results

Sprint 14 refined the complete Vue application without changing backend API
contracts or route behaviour. The work was delivered through five reviewable
increments covering the initial audit, shared design foundation, core
productivity workflows, planning and account workflows, and final validation.

Completed outputs:

- Replaced the prototype shell with responsive desktop and mobile navigation.
- Added shared typography, colour, spacing, focus, and state foundations.
- Refined Dashboard, Tasks, Recommendations, Study Plan, Handbook Import,
  Settings, Login, and Register.
- Added structural loading states and clearer empty, success, error, disabled,
  and refresh states.
- Added accessible icon controls, labelled form fields, keyboard focus return,
  and reduced-motion support.
- Corrected task-page overflow at 1024 px and handbook file-input overflow at
  390 px during final browser testing.

## Validation Results

Validation completed locally on 2026-08-10:

| Check | Result |
| --- | --- |
| Backend Maven test suite | 83 passed, 0 failed, 0 errors, 0 skipped |
| Frontend production build | Passed with Vite 8.1.0 |
| Frontend dependency audit | 0 vulnerabilities |
| Desktop browser review | All authenticated pages passed at 1440 px |
| Tablet browser review | All authenticated pages passed at 1024 px |
| Mobile browser review | Authenticated and authentication pages passed at 390 px |
| Full-stack smoke flow | Registration, authenticated navigation, task creation, and recommendation output passed |
| Accessibility review | No unnamed buttons, unlabelled form controls, or duplicate IDs remained |
| Browser console | No frontend warnings or errors during the final review |

The remaining workflow step is to merge the sprint branch into `develop`
through a pull request after CI passes.
