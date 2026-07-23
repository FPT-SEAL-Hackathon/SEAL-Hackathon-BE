# Conversation Log

## Summary of Recent Session (Date: 2026-07-22)

### 1. Merge Conflicts Resolved
- **Backend:** Fixed a conflict in `RoundServiceImpl.java`. Restored the correct `endDate` mapping from `HEAD` and merged the new `appealStartTime` and `appealEndTime` logic from the `dev` branch.
- **Frontend:**
  - `EventModal.tsx`: Merged UI component imports, preserving both the date picker components and the image cropper components.
  - `round.ts`: Merged `Round` and `RoundRequest` interfaces. Ensured new `appealStartTime` and `appealEndTime` properties are nullable (`string | null`) to match the backend's expected date typings, resolving the conflict where `dev` had strictly forced them to `string`.
  - `AdminAppealsView.tsx`: Resolved a minor `<StatusBadge />` syntax conflict.

### 2. Dependency Fixes
- Addressed an issue where Vite failed to resolve `"react-easy-crop"` in `ImageCropperModal.tsx`. It was determined that the `package.json` had the dependency (merged from `dev`) but it was not installed in `node_modules`. Instructed the user to run `npm install`.

### 3. Test Data Creation (Event & Round)
- Advised the user to use the Web UI to safely create test data (Event, Category, Round, and Event Criteria) instead of a raw SQL script. This prevents issues with UUID foreign key constraints and `CreatedByID` bindings. Provided standard timestamps and statuses (e.g., `Registration Open`, `Ongoing`) needed to immediately test the repository metadata submission feature.

### 4. Fix "Invalid JSON format" 400 Bad Request
- **Root Cause 1:** The `CreateEventRequest` payload in `EventModal.tsx` was sending empty strings `""` for optional fields like `registrationStart`, `registrationEnd`, and `eventStatusId` when they were empty. Jackson failed to parse `""` into `LocalDateTime` and `UUID`, resulting in a parse error.
  - **Fix 1:** Modified the payload to send `undefined` instead of `""`, allowing `JSON.stringify` to omit the fields so they parse as `null`, triggering the correct `@NotNull` Spring validation.
- **Root Cause 2:** `registrationStart` was being sent as `"2026-07-22T15:13:00:00"`. The `formatDateTime` function was blindly appending `:00` to date strings, corrupting strings that already contained seconds.
  - **Fix 2:** Refactored `formatDateTime` to only append `:00` if the string length is exactly 16 (i.e., missing seconds).

### 5. Customizations Updated
- User added a strict rule: "Database Table Names Are Not Java Symbols". Added this to `AGENTS.md`.
- Added new rules derived from the JSON parse errors to `AGENTS.md` regarding frontend payloads for `LocalDateTime` and `UUID`.

### 6. Organizer Date/Time Refactoring and Correction Pass
- **Audit & Timeline Fixes**: Audited all Organizer forms for date/time fields. Fixed `timelineUtils.ts` to construct local dates from `YYYY-MM-DD` strings to prevent UTC shift bugs.
- **Strict Boundary Implementation**: Enhanced `DateTimePickerField.tsx` with `strictMin` and `strictMax` props. This adjusts the internal bounds by exactly 1 minute to enforce strictly less-than/greater-than logic for minute-level dropdown selections.
- **Form-level Validation**: Removed dangerous auto-clearing `useEffect` hooks in shared pickers and relied on form-level validation. Added strict `AppealStartTime < AppealEndTime` checks in `RoundForm.tsx`.
- **Business Rule Correction**: Removed over-restrictive coupling where Registration dates were artificially bound to Event bounds in both `EventModal.tsx` and `EventServiceImplementation.java` (as registration is actually allowed to span into the active Event according to seed data). Removed Event-level bounds on `AppealEndTime` in `RoundServiceImpl.java`.

### 7. Final Static Review of Strict Date-Time Boundaries
- **Scope**: Reviewed the strict date-time boundary implementation without adding new features. Did not modify Schedule date-only behavior or repository integration code.
- **Minute Precision Confirmed**: `DateTimePickerField.tsx` parses and displays date-times at minute precision only. It emits values in `yyyy-MM-dd'T'HH:mm:00`, with seconds fixed to `00`.
- **Boundary Normalization Fix**: Updated `DateTimePickerField.tsx` so all `minDateTime` and `maxDateTime` values are cloned and normalized with `setSeconds(0, 0)` before applying `strictMin` (`+1 minute`) or `strictMax` (`-1 minute`). This avoids preserving hidden seconds/milliseconds from `new Date()` boundaries and avoids mutating incoming `Date` props.
- **Strict Boundary Inventory**:
  - `EventModal.tsx`: Registration Start uses `strictMax` against Registration End, enforcing `RegistrationStart < RegistrationEnd`.
  - `EventModal.tsx`: Registration End uses `strictMin` against Registration Start, enforcing `RegistrationEnd > RegistrationStart`.
  - `RoundForm.tsx`: Round Start uses `strictMax` against Round End, enforcing `RoundStart < RoundEnd`.
  - `RoundForm.tsx`: Round End uses `strictMin` only when bounded by Round Start, enforcing `RoundEnd > RoundStart`.
  - `RoundForm.tsx`: Appeal Start Time uses `strictMax` against Appeal End Time, enforcing `AppealStartTime < AppealEndTime`.
  - `RoundForm.tsx`: Appeal End Time uses `strictMin` against Appeal Start Time, enforcing `AppealEndTime > AppealStartTime`.
- **Inclusive Deadline Boundaries Confirmed**: `SubmissionDeadline` and `JudgingDeadline` do not use strict flags. Their existing rules remain inclusive: Submission Deadline is `>= RoundStart` and `<= JudgingDeadline` or `<= RoundEnd`; Judging Deadline is `>= SubmissionDeadline` or `>= RoundStart`, and `<= RoundEnd`.
- **Backend/Form Duplication Confirmed**: Registration, Round, Appeal, Submission Deadline, and Judging Deadline rules are enforced in form-level save validation and backend service validation, not only by disabled picker options. Backend event update validation was aligned to reject equality for `RegistrationStart < RegistrationEnd`.
- **Verification Status**:
  - Frontend `npx.cmd tsc --noEmit` failed due to current dirty-tree issues: duplicate `roundStatusId` in `RoundForm.tsx` and submission repository component API type errors.
  - Frontend `npm.cmd run build` passed with warnings, including the duplicate `roundStatusId` warning.
  - Backend `mvn.cmd -Dtest=EventServiceImplementationCreateTest test` compiled main sources but failed during test compilation because `RepositoryIntegrationServiceTest.java` imports a missing `core.exception.AccessDeniedException`.

### 8. Agent Reflection and Learnings Adopted
- Reviewed and extracted key problem-solving heuristics from another agent's execution transcript regarding the strict date-time boundary tasks.
- **Added "Agent Reasoning & Problem-Solving Guidelines" to `AGENTS.md`**, specifically noting:
  - **Dirty State Awareness**: Check `git status` first and minimize edit footprint in conflicting/dirty files.
  - **Validation Symmetry**: Ensure `Create` and `Update` backend routes enforce identical validation logic.
  - **Precision Normalization**: Always zero-out milliseconds/seconds before applying minute-level mathematical boundary offsets.
  - **Concrete Verification**: Fallback to native `.cmd` wrappers (`mvn.cmd`, `npx.cmd`) if standard PowerShell sandboxes fail, and accurately differentiate pre-existing compilation errors from new ones.

### 9. Further Test/Compile Stabilization Learnings
- Reviewed the final compile/test-compilation cleanup pass from another agent's execution.
- **Added further guidelines to `AGENTS.md`**, specifically noting:
  - **Untracked Incomplete Files**: Deleting untracked files causing TS errors when they are not imported by the main app, instead of faking their dependencies.
  - **Grounding Tests to Production Reality**: Updating outdated tests to match current production logic (e.g. exception types, method names, new behavior) rather than changing production logic to pass outdated tests.
  - **Test-Only Constructors**: Modifying production classes with package-private constructors to inject test clients (`RestClient`) when the public constructor builds them internally, ensuring mock servers like `MockRestServiceServer` bind correctly.
  - **Debugging Build Plumbing**: Identifying and fixing bugs inside build wrappers (like `mvnw.cmd` batch script errors) rather than treating them as code defects.

### 10. Dirty-Code Prevention Rule Added
- User requested that agents stop making the repository dirtier after the compile/test cleanup work.
- Added a dedicated `Dirty-Code Prevention and Cleanup Mode` section to `AGENTS.md`.
- New standing rule: documentation/log requests must only edit documentation/log files. Agents must not opportunistically fix code, tests, wrappers, imports, formatting, generated files, or conflict sections during documentation-only requests.
- New cleanup-mode rule: when the repo has `UU`, `MM`, or `AM` files, agents must edit only the exact files and exact verified failures named by the user. They must report unrelated dirty or unmerged files separately rather than broadening the task.
- Current repository state is intentionally still dirty:
  - Backend contains many pre-existing repository integration additions/modifications plus cleanup changes from the compile/test pass.
  - Frontend still has unmerged tracked files: `EventModal.tsx`, `src/features/events/types/round.ts`, and `AdminAppealsView.tsx`.
  - `conversation_log.md` remains untracked in the backend repo.

## Summary of Recent Session (Date: 2026-07-23)

### 1. Frontend Add User Dropdown Layering Fix
- Investigated a UI bug where Add/Edit User Role and Status dropdowns appeared behind the modal/table.
- Root cause:
  - `UserFormModal` used an overlay with `zIndex: 70`.
  - Radix `SelectContent` renders through a portal and defaulted below the modal layer.
  - The Role/Status selects were also using native `<option>` children inside Radix `Select`, which is not the correct Radix API.
- Fixed `SEAL-Hackathon-FE/src/pages/admin/components/AdminUsersView.tsx`:
  - Replaced native `<option>` children with `SelectItem` for Role and Status.
  - Added `className="z-[100]"` to the modal `FormSelect` `SelectContent`.
- Frontend verification:
  - `npx.cmd tsc --noEmit` still failed due to pre-existing unrelated errors in `LeaderDashboard.tsx`:
    - Missing `@/features/submissions/components/SubmissionRepositoryField`.
    - Missing `useAuth`.
    - One implicit `any` callback parameter.

### 2. Event/Round Date-Time Business Logic Audit
- Investigated a backend 400 response:
  - `Round start date cannot be before event start date`
  - Path: `/api/v1/round/{roundId}`
- Root cause:
  - Backend treats event schedule as date-only (`LocalDate`) and round/deadline/appeal fields as date-time (`LocalDateTime`).
  - Backend correctly expands the event range to `eventStartDate 00:00` through `eventEndDate 23:59:59...`.
  - Frontend edit-round flow did not pass the parent `event` from `RoundCard` into `RoundForm`, so edit mode lost event boundary picker constraints and form-level event validation. The backend then became the first layer to reject invalid round dates.
- Business-rule review:
  - Event range is date-only and inclusive.
  - Registration is strict internally: `registrationStart < registrationEnd`.
  - Registration may end on the event start date; backend should compare registration end by date, not against `eventStartDate.atStartOfDay()`.
  - Round range is strict internally: `roundStart < roundEnd`.
  - Round must stay inside the event date-only range.
  - Submission and judging deadlines remain inclusive inside the round.
  - Appeal follows the contest flow: `judgingDeadline <= appealStartTime < appealEndTime <= roundEnd` when judging/round bounds exist, and appeal stays inside the event range.

### 3. Event/Round Date-Time Fixes Applied
- Backend changes:
  - `EventServiceImplementation.java`
    - Create event now rejects `registrationEnd.toLocalDate() > eventStartDate`.
    - Update event now also compares registration end by date instead of rejecting same-day times after midnight.
  - `RoundServiceImpl.java`
    - Added explicit comment that event dates are date-only while round/appeal windows may use any minute inside those days.
    - Enforced appeal start/end inside the event date-only range.
    - Enforced appeal start after or equal to judging deadline when present.
    - Enforced appeal start after or equal to round start when judging deadline is absent.
    - Enforced appeal end before or equal to round end.
    - Preserved strict `appealStartTime < appealEndTime`.
- Frontend changes:
  - `DateTimePickerField.tsx`
    - Date-only `minDateTime` now means `00:00`.
    - Date-only `maxDateTime` now means `23:59`.
    - Minute precision remains unchanged.
  - `RoundTab.tsx` and `RoundCard.tsx`
    - Parent event is now passed into edit-round `RoundForm`, not just add-round `RoundForm`.
  - `RoundForm.tsx`
    - Added form-level appeal validation matching backend rules.
    - Appeal picker bounds now account for judging deadline, round start/end, and event start/end.
- Backend tests added/updated:
  - `EventServiceImplementationCreateTest`
    - Added rejection for registration ending after event start date.
    - Preserved same-date registration end as valid.
  - `RoundServiceValidationTest`
    - Added rejection for appeal start before judging deadline.
    - Added rejection for appeal end after round end.
    - Added valid case for a round ending at the last minute of the event end date.

### 4. Verification and Current Dirty State
- Backend focused verification passed:
  - Command: `mvn "-Dtest=RoundServiceValidationTest,EventServiceImplementationCreateTest" test`
  - Result: exit code 0, 16 tests run, 0 failures, 0 errors.
- Frontend TypeScript verification still fails due to unrelated pre-existing `LeaderDashboard.tsx` errors listed above.
- Current touched files from the 2026-07-23 work:
  - Backend:
    - `AGENTS.md`
    - `conversation_log.md`
    - `src/main/java/com/fpt/swp/sealhackathonbe/event/service/impl/EventServiceImplementation.java`
    - `src/main/java/com/fpt/swp/sealhackathonbe/round/service/impl/RoundServiceImpl.java`
    - `src/test/java/com/fpt/swp/sealhackathonbe/event/service/impl/EventServiceImplementationCreateTest.java`
    - `src/test/java/com/fpt/swp/sealhackathonbe/round/service/impl/RoundServiceValidationTest.java`
  - Frontend:
    - `src/pages/admin/components/AdminUsersView.tsx`
    - `src/features/events/components/round/RoundCard.tsx`
    - `src/features/events/components/round/RoundForm.tsx`
    - `src/features/events/components/round/RoundTab.tsx`
    - `src/features/events/shared/ui/DateTimePickerField.tsx`
- `mvnw.cmd` remains a dirty backend file from earlier work and was not part of the documentation update request.

### 5. AGENTS.md Updates Added
- Added Radix Select modal layering guidance:
  - Radix `SelectContent` is portal-rendered and must have a z-index above modal overlays.
  - Radix `Select` must use `SelectItem`, not native `<option>`.
- Added event date-only / round date-time boundary guidance:
  - Date-only event boundaries must expand to start/end of day in datetime pickers.
  - Add and edit round forms must both receive parent event context.
  - Documented current event, registration, round, deadline, and appeal business rules.

## Summary of Recent Session (Date: 2026-07-23) — Submission-level GitHub Repository Metadata

### 1. Database Clarification
- Confirmed the project runs on **Microsoft SQL Server (local dev)**, migrated off Azure SQL after the budget ran out. "msql" means MSSQL, not MySQL. No driver/dialect change — only `DB_URL` points at the local server; migrations stay T-SQL.
- Updated `AGENTS.md` Project Facts accordingly.

### 2. Audit Before Implementation (Phase 0–3)
- Verified clean baseline better than the previous log implied: backend `compile` + `test-compile` both exit 0, frontend `tsc --noEmit` exit 0. Earlier blockers (`RepositoryIntegrationServiceTest` missing import, `LeaderDashboard` missing `SubmissionRepositoryField`/`useAuth`) were already resolved.
- Found the feature ~70% done on backend (Step 2/3 complete, Step 4 running but flawed) and almost nothing on frontend (only API service methods, no UI components).
- Identified the key gaps: over-broad authorization (`eventRepository.findAll()` let any event creator view/resync ANY submission), GitHub call inside the submit transaction, fetch errors swallowed instead of persisted, no concurrency guard, no Organizer overview/export.

### 3. Backend Changes
- `SubmissionRepositoryService.java`: rewrote authorization to scope Organizer to the submission's OWN event (`EventRepository.existsByEventIdAndCreatedBy_UserId`); team member or assigned judge (view-only) also allowed; judge cannot resync. Added Organizer event overview + CSV export (CSV-injection-safe, UTF-8 BOM), atomic `RUNNING` sync lock with 409 `REPOSITORY_SYNC_ALREADY_RUNNING` and a 2-minute stale takeover, and `RepositoryMetadataFetchResult` so failed fetches persist `FAILED` + safe error code.
- `SubmissionCommandServiceImpl.submitWork`: GitHub fetch now runs OUTSIDE the DB transaction; a `TransactionTemplate` commits SP upsert + history + metadata atomically. Deadline re-checked inside the transaction.
- `SubmissionQueryServiceImpl`: list endpoints use a batched `findBySubmission_SubmissionIdIn` to avoid N+1.
- `GlobalExceptionHandler`: added a handler for `RepositoryMetadataException` (was falling through to 500) and the missing `SUBMISSION_*` status mappings.
- New files: `RepositoryMetadataFetchResult`, `EventSubmissionRepositoryItemResponse`, `EventSubmissionRepositoryController`. Entity/mapper/response gained `starCount`/`forkCount`/`openIssuesCount`.
- New migration `20260723_submission_repositories_counts.sql` (adds the three count columns).

### 4. Frontend Changes
- New shared `SubmissionRepositoryField` + `RepositoryMetadataCard` (`src/features/submissions/components/`). Team form sends only `repositoryUrl` (backend always re-fetches); preview clears on URL change; refresh only when submission editable.
- Judge: read-only `RepositoryMetadataCard` in `JudgeScoringView` with the "metadata does not determine score" disclaimer, no edit/resync controls.
- Organizer: new `AdminSubmissionRepositoriesView` (nav key `submission-repositories`) with Team/Round/Category/Language/Status filters, per-row resync, CSV export, and a neutral "last push after deadline" indicator. Legacy PAT view relabeled "Repository Integrations (Legacy)".
- `submissionService.ts`, `navigation.ts`, `permissions.ts`, `AdminDashboard.tsx` wired up.

### 5. Verification
- Backend: `sh ./mvnw test` → 130 tests, 0 failures (fixed one new test that leaked a mocked `SecurityContext` by using `createEmptyContext`). New `SubmissionRepositoryServiceTest` covers authz (member/organizer-same-event/organizer-other-event/assigned-judge/judge-resync-denied), persistence (create/update-no-duplicate/failed-keeps-metadata), and 409 conflict.
- Frontend: `npx.cmd tsc --noEmit` exit 0; `npm.cmd run build` exit 0 (pre-existing chunk-size warning only).

### 6. Cleanup
- Removed the untracked `powershell.bat` shim in both repos and reverted the local `mvnw.cmd` edits (the patch was backed up to the session scratchpad). Build still works via `sh ./mvnw`.

### 7. Remaining Limitations
- No manual end-to-end UI pass yet (needs the app + a migrated local DB). GitLab stays enum-only (future-compatible). Export is CSV-only. Migrations `20260722_submission_repositories.sql` + `20260723_submission_repositories_counts.sql` must be run manually on the local DB.
