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
