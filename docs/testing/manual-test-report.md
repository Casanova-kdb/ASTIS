# ASTIS Manual Test Report

## Purpose

This document records manual full-stack testing for the ASTIS MVP.

The goal is to verify that the frontend, backend APIs, authentication, task management, analytics, recommendation ranking, and AI study advice work together as one complete system.

## Test Environment

| Item            | Value                                |
| --------------- | ------------------------------------ |
| Test date       | 2026/06/27                           |
| Tester          | Brayden Ji / Codex-assisted manual test |
| Backend URL     | `http://localhost:8080/api`          |
| Frontend URL    | `http://localhost:5173`              |
| Database        | MySQL local `astis`                  |
| Browser         | Codex in-app browser                 |
| Backend branch  | `sprint/3-frontend-prototype`        |
| Frontend branch | `sprint/3-frontend-prototype`        |

## Test Account

| Field    | Value                                         |
| -------- | --------------------------------------------- |
| Email    | `manual_1782557449483@astis.local`, `demo.student@astis.local` |
| Password | `12345678`                                                   |
| Notes    | A temporary account was used for CRUD testing. The local demo account was used for dashboard and recommendation data. |

## Test Status Legend

| Status  | Meaning                                            |
| ------- | -------------------------------------------------- |
| PASS    | Works as expected                                  |
| FAIL    | Does not work as expected                          |
| PARTIAL | Works partly, but has issues                       |
| BLOCKED | Cannot test due to environment or dependency issue |

## Test Cases

### TC-001 Register New User

| Field           | Details                                                                                  |
| --------------- | ---------------------------------------------------------------------------------------- |
| User Story      | US-001 Register Account                                                                  |
| Preconditions   | Backend and frontend are running. User is not logged in.                                 |
| Steps           | Open `/register`, enter username, email, and password, then submit the form.             |
| Expected Result | Account is created successfully, JWT is stored, and the user is redirected to Dashboard. |
| Actual Result   | Registered `manual_1782557449483@astis.local` and redirected to Dashboard.              |
| Status          | PASS                                                                                     |
| Notes           | JWT was stored after registration.                                                       |

### TC-002 Login Existing User

| Field           | Details                                                               |
| --------------- | --------------------------------------------------------------------- |
| User Story      | US-002 Login Account                                                  |
| Preconditions   | Test account already exists. User is logged out.                      |
| Steps           | Open `/login`, enter email and password, then submit the form.        |
| Expected Result | User logs in successfully, JWT is stored, and Dashboard is displayed. |
| Actual Result   | Logged in with `manual_1782557449483@astis.local` and redirected to Dashboard. |
| Status          | PASS                                                                  |
| Notes           | Login form and JWT authentication worked correctly.                    |

### TC-003 Protected Route Redirect

| Field           | Details                                       |
| --------------- | --------------------------------------------- |
| User Story      | US-002 Login Account                          |
| Preconditions   | User is logged out.                           |
| Steps           | Open `/tasks` or `/recommendations` directly. |
| Expected Result | User is redirected to `/login`.               |
| Actual Result   | Unauthenticated access to `/tasks` redirected to `/login?redirect=/tasks`. |
| Status          | PASS                                          |
| Notes           | Route guard worked as expected.               |

### TC-004 Logout

| Field           | Details                                         |
| --------------- | ----------------------------------------------- |
| User Story      | US-002 Login Account                            |
| Preconditions   | User is logged in.                              |
| Steps           | Click Logout from the sidebar.                  |
| Expected Result | JWT is removed and user is redirected to Login. |
| Actual Result   | Logout redirected the user to `/login`.         |
| Status          | PASS                                            |
| Notes           | JWT was removed from local storage.             |

### TC-005 Create Task

| Field           | Details                                                                                  |
| --------------- | ---------------------------------------------------------------------------------------- |
| User Story      | US-006 Create Task                                                                       |
| Preconditions   | User is logged in and opens the Tasks page.                                              |
| Steps           | Fill in title, description, task type, priority, deadline, estimated hours, then submit. |
| Expected Result | New task is created and appears in the task list.                                        |
| Actual Result   | Task was created and appeared in the task list.                                         |
| Status          | PASS                                                                                     |
| Notes           | Tested with a temporary task created from the frontend form.                            |

### TC-006 View Task List

| Field           | Details                                                                                         |
| --------------- | ----------------------------------------------------------------------------------------------- |
| User Story      | US-007 View Task List                                                                           |
| Preconditions   | User has at least one task.                                                                     |
| Steps           | Open the Tasks page.                                                                            |
| Expected Result | User can see their own tasks with title, type, priority, status, deadline, and estimated hours. |
| Actual Result   | Task list showed title, type, priority, status, deadline, and estimated hours.                 |
| Status          | PASS                                                                                            |
| Notes           | Task data matched the created record.                                                          |

### TC-007 Update Task Details

| Field           | Details                                                        |
| --------------- | -------------------------------------------------------------- |
| User Story      | US-008 Update Task Details                                     |
| Preconditions   | User has at least one task.                                    |
| Steps           | Click Edit on a task, change task details, then save changes.  |
| Expected Result | Updated task details are saved and displayed in the task list. |
| Actual Result   | Task details were updated and displayed in the task list.      |
| Status          | PASS                                                           |
| Notes           | Edited title, priority, and estimated hours.                   |

### TC-008 Update Task Status

| Field           | Details                                                             |
| --------------- | ------------------------------------------------------------------- |
| User Story      | US-009 Update Task Status                                           |
| Preconditions   | User has at least one task.                                         |
| Steps           | Change task status from Pending to In Progress or Completed.        |
| Expected Result | Task status updates successfully and remains updated after refresh. |
| Actual Result   | Task status changed to Completed and was confirmed by the Completed status filter before deletion. |
| Status          | PASS                                                                |
| Notes           | Status update persisted before the task was deleted.                |

### TC-009 Filter Tasks By Status

| Field           | Details                                                  |
| --------------- | -------------------------------------------------------- |
| User Story      | US-007 View Task List                                    |
| Preconditions   | User has tasks with different statuses.                  |
| Steps           | Select a status filter on the Tasks page.                |
| Expected Result | Task list only shows tasks matching the selected status. |
| Actual Result   | Completed filter showed the completed task.              |
| Status          | PASS                                                     |
| Notes           | Status filter returned the expected task.                |

### TC-010 Delete Task

| Field           | Details                                                                 |
| --------------- | ----------------------------------------------------------------------- |
| User Story      | US-010 Delete Task                                                      |
| Preconditions   | User has at least one task.                                             |
| Steps           | Click Delete on a task and confirm deletion.                            |
| Expected Result | Task is removed from the task list and does not reappear after refresh. |
| Actual Result   | Task was deleted and no longer appeared after returning to the Tasks page. |
| Status          | PASS                                                                    |
| Notes           | Delete confirmation was completed during testing.                        |

### TC-011 Dashboard Summary

| Field           | Details                                                                                             |
| --------------- | --------------------------------------------------------------------------------------------------- |
| User Story      | US-014 Basic Analytics Summary                                                                      |
| Preconditions   | User has task records.                                                                              |
| Steps           | Open Dashboard.                                                                                     |
| Expected Result | Dashboard displays total tasks, completed tasks, pending tasks, overdue tasks, and completion rate. |
| Actual Result   | Dashboard displayed summary metric cards.                                                       |
| Status          | PASS                                                                                                |
| Notes           | Tested with local demo account data.                                                               |

### TC-012 Dashboard Top Recommended Task

| Field           | Details                                                                                        |
| --------------- | ---------------------------------------------------------------------------------------------- |
| User Story      | US-017 Recommended Task Ordering                                                               |
| Preconditions   | User has pending or in-progress tasks.                                                         |
| Steps           | Open Dashboard and check Today Focus section.                                                  |
| Expected Result | Dashboard displays the highest ranked recommended task with score, risk, reason, and deadline. |
| Actual Result   | Dashboard displayed the top recommended task in the Today Focus section.                        |
| Status          | PASS                                                                                           |
| Notes           | The card included score, risk, reason, and deadline information.                                |

### TC-013 Recommendation Ranking

| Field           | Details                                                                                                           |
| --------------- | ----------------------------------------------------------------------------------------------------------------- |
| User Story      | US-017 Recommended Task Ordering                                                                                  |
| Preconditions   | User has tasks that can be ranked.                                                                                |
| Steps           | Open Recommendations page.                                                                                        |
| Expected Result | Page displays ranked tasks with rank position, priority score, delay risk, reason, deadline, and estimated hours. |
| Actual Result   | Recommendations page displayed ranked tasks with rank, priority score, delay risk, reason, deadline, and estimated hours. |
| Status          | PASS                                                                                                              |
| Notes           | Tested with `demo.student@astis.local`.                                                                           |

### TC-014 AI Study Advice

| Field           | Details                                                                  |
| --------------- | ------------------------------------------------------------------------ |
| User Story      | US-018 AI Study Advice                                                   |
| Preconditions   | Backend AI configuration is available, or fallback advice is enabled.    |
| Steps           | Open Recommendations page and check AI Study Advice panel.               |
| Expected Result | Page displays study advice generated from the current recommended tasks. |
| Actual Result   | AI study advice panel displayed advice based on recommended tasks.       |
| Status          | PASS                                                                     |
| Notes           | Local fallback advice was shown because no DeepSeek API key was supplied for this automated test run. |

### TC-015 Backend Unavailable Error Handling

| Field           | Details                                                       |
| --------------- | ------------------------------------------------------------- |
| User Story      | General MVP reliability                                       |
| Preconditions   | Frontend is running. Backend is stopped.                      |
| Steps           | Open Dashboard, Tasks, or Recommendations page.               |
| Expected Result | Frontend shows a clear error message instead of a blank page. |
| Actual Result   | Frontend displayed an error message when the backend was unavailable. |
| Status          | PASS                                                          |
| Notes           | Backend was stopped temporarily to test error handling.       |

## Issues Found

| ID   | Issue | Severity | Related Test Case | Status |
| ---- | ----- | -------- | ----------------- | ------ |
| None | No blocking issues found during this manual test run. | N/A | All | Closed |

## Summary

| Metric           | Count |
| ---------------- | ----- |
| Total test cases | 15    |
| Passed           | 15    |
| Failed           | 0     |
| Partial          | 0     |
| Blocked          | 0     |

## Final Notes

All planned full-stack manual test cases passed in the local environment.

The MVP flow worked from browser login through task management, dashboard analytics, recommendation ranking, and AI study advice. The frontend also displayed an error message when the backend was unavailable.
