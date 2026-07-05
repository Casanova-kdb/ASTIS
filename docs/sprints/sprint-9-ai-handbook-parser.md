# Sprint 9: AI Handbook Parser

## Goal

Add a post-MVP AI workflow that helps students turn module handbook files into task drafts.

This sprint focuses on file upload, temporary text extraction, AI-assisted parsing, and user review before creating real tasks.

## Scope

In scope:

- Upload PDF or DOCX module handbook files
- Extract readable text on the backend
- Use DeepSeek to identify possible academic tasks when configured
- Use a local fallback parser when DeepSeek is not configured
- Return editable task drafts instead of directly creating tasks
- Let the frontend create confirmed drafts as normal tasks
- Keep unreadable or uncertain fields empty for user completion
- Use extracted late-submission or extension information to support overdue-task ranking decisions

Out of scope:

- Permanent PDF/DOCX storage
- Cloud bucket storage
- Automatic task creation without user confirmation
- Full study planning agent behaviour

## Design Decision

Uploaded handbook files are processed temporarily and are not stored permanently in the MVP/post-MVP implementation.

The system stores only the final user-confirmed tasks. This reduces privacy risk and keeps the feature focused on the core academic workflow.

## Backend Output

New API:

```text
POST /api/handbooks/parse
```

Request:

```text
multipart/form-data
file: PDF or DOCX
```

Response:

- uploaded filename
- parser provider
- extracted text preview
- draft task list
- deadline missing flag
- confidence score
- source evidence

Important parsing rule:

If the AI/parser can clearly read a field from the handbook, it returns the value. If it cannot clearly read the field, the response keeps that field empty instead of inventing a default value.

Overdue-task rule:

Overdue tasks are not automatically treated as the highest-priority work. The recommendation score is reduced for overdue tasks because the original deadline has already been missed. If the handbook or task criteria indicate that late submission, extension, or resubmission may be possible, the score is reduced less aggressively.

## Frontend Output

New page:

```text
/handbook-import
```

The user can:

- upload a handbook file
- review extracted task drafts
- edit task title, description, priority, deadline, and estimated hours
- create confirmed tasks through the existing task API

## Acceptance Notes

- The feature does not create tasks automatically.
- If a deadline is missing, the frontend requires the user to add one before creating the task.
- Empty extracted fields are shown as blank inputs so the user can complete them manually.
- Dates before 2027 are adjusted to 2027 for local testing.
- The fallback parser keeps the feature demoable without an AI API key.

## Status

Implemented in branch:

```text
sprint/9-ai-handbook-parser
```
