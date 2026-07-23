# Conversation Log

> Ghi chú: file này là log dev cục bộ, đã được `.gitignore` (bỏ track ở commit `435cc7d`).
> File từng bị xóa khỏi thư mục làm việc và được tạo lại; nội dung các session trước được khôi phục từ lịch sử hội thoại.

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
- **Boundary Normalization Fix**: Updated `DateTimePickerField.tsx` so all `minDateTime` and `maxDateTime` values are cloned and normalized with `setSeconds(0, 0)` before applying `strictMin` (`+1 minute`) or `strictMax` (`-1 minute`).
- **Strict Boundary Inventory**: Registration Start/End, Round Start/End, Appeal Start/End use strict flags; Submission/Judging deadlines remain inclusive.
- **Backend/Form Duplication Confirmed**: Rules enforced in form-level save validation and backend service validation, not only by disabled picker options.

### 8. Agent Reflection and Learnings Adopted
- Added "Agent Reasoning & Problem-Solving Guidelines" to `AGENTS.md` (Dirty State Awareness, Validation Symmetry, Precision Normalization, Concrete Verification).

### 9. Further Test/Compile Stabilization Learnings
- Added guidelines to `AGENTS.md`: Untracked Incomplete Files, Grounding Tests to Production Reality, Test-Only Constructors, Debugging Build Plumbing.

### 10. Dirty-Code Prevention Rule Added
- Added a dedicated `Dirty-Code Prevention and Cleanup Mode` section to `AGENTS.md`. Documentation/log requests must only edit documentation/log files.

## Summary of Recent Session (Date: 2026-07-23)

### 1. Frontend Add User Dropdown Layering Fix
- Fixed Add/Edit User Role/Status dropdowns rendering behind the modal in `AdminUsersView.tsx`: replaced native `<option>` with `SelectItem`, added `z-[100]` to the modal `SelectContent`.

### 2-5. Event/Round Date-Time Business Logic
- Backend: registration may end on the event start date (compare by date); appeal validation inside event/round bounds in `RoundServiceImpl.java`.
- Frontend: `DateTimePickerField` date-only bounds expand to 00:00 / 23:59; edit-round forms now receive parent `event`; appeal form-level validation added.
- Tests: `EventServiceImplementationCreateTest`, `RoundServiceValidationTest` updated (16 tests pass).
- AGENTS.md updated with Radix Select layering + event date-only / round date-time boundary guidance.

## Summary of Recent Session (Date: 2026-07-23) — Submission Repository Metadata Feature + Schedule Timeline Fix

### 1. Audit hiện trạng
- Rà soát BE/FE/migrations cho tính năng GitHub repository metadata cấp submission. Xác định backend đã có sẵn Step 2/3 (bảng `SubmissionRepositories`, `GitHubRepositoryMetadataClient`), Step 4 chạy được nhưng còn lỗ hổng; frontend gần như chưa có UI.
- Verify baseline: `sh ./mvnw compile`/`test-compile` exit 0, `npx.cmd tsc --noEmit` exit 0.

### 2. Backend — hoàn thiện tính năng
- **Authorization scope theo event**: `EventRepository.existsByEventIdAndCreatedBy_UserId` thay cho `findAll()`; chỉ team member / assigned judge (chỉ xem) / organizer của đúng event. Judge không được resync.
- **Tách GitHub call ra ngoài transaction**: `SubmissionCommandServiceImpl.submitWork` fetch metadata trước, rồi `TransactionTemplate` commit atomic (SP upsert + history + metadata).
- **Lưu trạng thái FAILED**: DTO mới `RepositoryMetadataFetchResult`; mapper `applyFetchResult` lưu error code/message an toàn thay vì nuốt lỗi.
- **Chống resync đồng thời**: `markSyncRunning` (RUNNING lock atomic) → 409 `REPOSITORY_SYNC_ALREADY_RUNNING`, stale timeout 2 phút; `failRunningSync` nhả lock khi lỗi.
- **Organizer overview + export**: `GET /api/v1/events/{eventId}/submission-repositories` và `/export` (CSV chống injection, BOM UTF-8), creator-only, batch query tránh N+1, cờ `lastPushAfterDeadline`.
- **Exception handler**: xử lý `RepositoryMetadataException` (trước rơi vào 500) + mapping các mã `SUBMISSION_*`.
- **Thêm cột** `StarCount`/`ForkCount`/`OpenIssuesCount` (entity + mapper + response + migration `20260723_submission_repositories_counts.sql`).
- **Test**: `SubmissionRepositoryServiceTest` (authz, persistence, conflict 409). `sh ./mvnw test` → 130 tests, 0 failures.

### 3. Frontend — UI theo vai trò
- **Team**: `SubmissionRepositoryField` + `RepositoryMetadataCard` (validate preview không lưu, submit chỉ gửi repositoryUrl); tích hợp `LeaderDashboard`, hiển thị metadata + Refresh khi round còn mở.
- **Judge**: metadata read-only trong `JudgeScoringView` + disclaimer không quyết định điểm.
- **Organizer**: `AdminSubmissionRepositoriesView` (nav `submission-repositories`): bảng + filter + resync + export; view PAT cũ đổi nhãn "Repository Integrations (Legacy)".
- Verify: `tsc` exit 0, `npm run build` exit 0.

### 4. Cleanup + commit guidance
- Xóa `powershell.bat` (shim rác, cả BE/FE), revert `mvnw.cmd` về bản gốc (patch backup ở scratchpad).
- Cung cấp nội dung commit tiếng Việt (BE + FE bao trùm). User đã commit (`852dc578` FE, các commit repository metadata BE).

### 5. Schedule Timeline Fix (FE)
- **Lỗi lệch trục**: `calculateTimelineBounds` trong `timelineUtils.ts` dùng `new Date()` cho ngày date-only của event → UTC midnight, lệch ~7h so với `getPercentage` (dùng `parseSafeDate`). Sửa để dùng `parseSafeDate` cho mọi mốc → bar/tick/round block khớp trục.
- **Edit round không hiện Schedule**: `RoundCard` nhánh edit chưa truyền `categories`/`categoryId` nên Timeline Preview bị ẩn. Thêm truyền `categories`/`allRounds`/`categoryId`/`editingRoundId`; `RoundForm` thêm prop `editingRoundId` và lọc round đang sửa khỏi preview (tránh trùng).
- Commit FE `4d0f798`. Verify `tsc`/`build` exit 0.

### 6. Q&A / tư vấn (không đổi code)
- `REPOSITORY_TOKEN_ENCRYPTION_KEY`: là khóa AES-256-GCM cho PAT của integration legacy; chuỗi toàn `A` decode ra đúng 32 byte (hợp lệ format, app boot được) nhưng toàn số 0 → chỉ dùng local, prod phải `openssl rand -base64 32`.
- `Create Database.sql`: là snapshot SSMS (UTF-16), **thiếu** 4 migration mới nhất (repository_integrations, ai_knowledge_base, submission_repositories, counts). Cơ chế: snapshot nền + migration chạy tay, không Flyway. Không nên sửa tay file UTF-16; regenerate bằng SSMS hoặc giữ migration làm nguồn chuẩn.
- Bảng đã làm việc (repository metadata): write `SubmissionRepositories`/`Submissions`/`SubmissionHistory`; read `Events`/`Rounds`/`Teams`/`TeamMembers`/`RoundJudges`/`Users`/`Categories`; legacy chỉ đánh dấu `RepositoryIntegrations`/`Repositories`/`RepositoryIssues`/`RepositorySyncLogs`.
- Thiết kế `SubmissionRepositories`: cần bảng riêng vì filter được (language/status), có state-machine sync + khóa concurrency + error transparency + tương lai GitLab. Cột `Repo*` cũ trên `Submissions` giờ gần như dư (chỉ còn sync `RepositoryURL`).
