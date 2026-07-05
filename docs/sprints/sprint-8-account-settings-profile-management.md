# Sprint 8: Account Settings and Profile Management

## Sprint Goal

Extend the Settings area so users can manage basic account details and change their password.

This sprint builds on Sprint 7. Sprint 7 added study profile settings, while Sprint 8 completes the account management side of the Settings page.

## Sprint Branch

```text
sprint/8-account-settings-profile-management
```

## User Stories Covered

- US-004 Manage User Profile

## Scope

Included in this sprint:

- View current account information
- Update display name
- Change password
- Verify current password before changing password
- Store new password as a hash
- Add account settings UI
- Add password form UI

Not included in this sprint:

- Email change
- Username change
- Account deletion
- Password reset by email
- Multi-factor authentication

## Backend Work

- Added account APIs:
  - `GET /api/users/me`
  - `PUT /api/users/me`
  - `PUT /api/users/me/password`
- Added account request DTOs.
- Added account service layer.
- Added password verification using the existing password encoder.
- Kept username and email read-only for this sprint.

## Frontend Work

- Extended Settings page with Account Settings section.
- Added read-only username and email fields.
- Added editable display name field.
- Added password update form.
- Updated stored frontend user data after display name changes.
- Updated sidebar display to prefer display name.

## Security Notes

The password update flow requires the current password.

The backend does not store the new password directly. It uses the existing password encoder so the database stores a password hash.

Email and username editing are intentionally excluded because they affect login identity, uniqueness validation, and JWT identity assumptions.

## Definition of Done

Sprint 8 is complete when:

- Users can view their current account information.
- Users can update display name.
- Users can change password with the correct current password.
- Wrong current password returns an error.
- New password can be used to log in.
- Settings page contains both account settings and study profile settings.
- Sprint documentation is updated.
