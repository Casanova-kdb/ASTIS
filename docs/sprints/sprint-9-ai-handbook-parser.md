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
- fallback reason when the local parser is used
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
- Dates are preserved exactly as stated in the handbook, including dates that are already in the past.
- The fallback parser keeps the feature demoable without an AI API key.

## Sprint 9 Fix Review

The first implementation temporarily changed dates before 2027 to 2027 so an older coursework handbook could be used for local testing.

This behaviour was removed during Sprint 9 review because test-specific date rewriting should not be part of production logic. The parser now preserves the source date and lets the task and recommendation modules handle overdue deadlines.

Fallback behaviour was also made more visible:

- The API returns a safe `fallbackReason` when DeepSeek is not configured or cannot return usable task drafts.
- The frontend displays the fallback reason below the parser summary.
- The backend records a warning without logging the API key or handbook content.

## Testing and Verification

Automated backend verification covers:

- PDF text extraction
- DOCX text extraction
- Empty file rejection
- Unsupported extension rejection
- File size limit validation
- Authenticated handbook API access
- Unauthenticated upload rejection
- Local fallback when DeepSeek is not configured
- Local fallback when DeepSeek returns invalid JSON
- Clear fallback reason in the API response
- Missing deadline handling
- Preservation of past deadlines
- Late-submission policy extraction
- User review before task creation
- Confirmed draft creation through the existing Task API

Verification commands:

```bash
mvn -q -f backend/pom.xml test
npm run build --prefix frontend
```

Backend result on 2026-07-22:

```text
Total backend tests: 49
Passed: 49
Failed: 0

Handbook-related tests: 15
Passed: 15
Failed: 0
```

## Known Limitations

- The AI prompt uses the first 12,000 extracted characters, so very long handbooks may require section-aware parsing later.
- The local fallback parser returns one basic draft and is less capable than DeepSeek.
- File extension and size validation are included, while file-signature and malware scanning are deferred to security hardening work.
- A broader AI evaluation dataset is deferred to a later recommendation and AI evaluation sprint.

## Definition of Done

Sprint 9 is complete when:

- Authenticated users can upload PDF and DOCX handbooks.
- Uploaded files are processed temporarily and are not stored permanently.
- DeepSeek or the local fallback returns editable task drafts.
- Missing fields remain empty instead of receiving invented values.
- Source dates are preserved without test-specific rewriting.
- Fallback use is clearly reported to the user.
- Users confirm drafts before tasks are created.
- Handbook tests and the full backend test suite pass.
- The frontend production build passes.

## Status

Implemented in the feature branch and hardened in the fix branch:

```text
sprint/9-ai-handbook-parser
fix/sprint-9-handbook-parser
```

## Next Sprint

Sprint 10 should focus on recommendation query performance and Redis caching. Recommendation SQL optimisation is intentionally kept outside Sprint 9 so the handbook parser sprint retains a clear scope.
