# Sprint 4: Full-stack Integration Polish

## Sprint Goal

Improve the ASTIS MVP after Sprint 3 by polishing frontend user experience and making authentication error handling more reliable.

This sprint focuses on small but important integration improvements rather than adding new major features. The goal is to make the existing MVP easier to test, demonstrate, and use.

## Sprint Branch

```text
sprint/4-auth-behavior
```

## Focus Areas

- Frontend user feedback
- Form layout responsiveness
- Authentication error handling
- MVP stability after full-stack integration

## Completed Work

- Added success feedback after task actions:
  - task created
  - task updated
  - task status updated
  - task deleted
- Fixed the task form layout issue where the Deadline and Estimated Hours inputs could overlap on narrower widths.
- Improved form row responsiveness so inputs wrap safely when space is limited.
- Added shared frontend handling for `401 Unauthorized` API responses.
- Cleared stored JWT and user data when the backend rejects authentication.
- Redirected users back to the Login page when their token is expired, invalid, or rejected.
- Preserved the current route as a redirect query after unauthorized access.

## Issues Fixed

### Task Form Input Overlap

The Deadline and Estimated Hours inputs could visually overlap when the form column was narrow.

Fix:

- Added `min-width: 0` to form labels and form controls.
- Changed the task form row layout to use responsive wrapping.
- Verified that the inputs no longer overlap in the browser.

### Unauthorized API Responses

Before this sprint, an expired or invalid JWT could leave the user on a protected page with only an API error.

Fix:

- Added a shared unauthorized handler to the frontend API client.
- When an API response returns `401`, the frontend clears stored auth data and redirects to Login.

## Testing and Verification

Verification completed during this sprint:

- Ran frontend production build:

```bash
npm run build
```

- Checked the Task form layout in the browser.
- Confirmed the Deadline and Estimated Hours inputs no longer overlap.
- Confirmed the frontend build still passes after adding `401` handling.

The existing full-stack manual test report remains available at:

```text
docs/testing/manual-test-report.md
```

## Known Limitations

- Automated frontend tests have not been added yet.
- UI polish is still MVP-level and can be improved further.
- Deployment has not been completed yet.
- The AI advice feature still depends on DeepSeek configuration when external AI output is required.

## Definition of Done

Sprint 4 is complete when:

- Task actions provide clear success feedback.
- Task form fields do not overlap in the tested layout.
- Unauthorized API responses clear local authentication state.
- Unauthorized users are redirected back to Login.
- The frontend still builds successfully.
- Sprint 4 changes are documented.

## Next Sprint

The next sprint should focus on final project preparation, including screenshots, final documentation review, deployment notes, and optional frontend test automation.
