# Tong hop API Backend SEAL Hackathon Manager

Tai lieu nay tong hop API tu source backend Spring Boot cua du an SEAL Hackathon Manager. Backend quan ly cac luong chinh cua hackathon: xac thuc tai khoan, quan ly su kien, category/round, team, dang ky tham gia, nop bai, cham diem, ranking, giai thuong, notification, consultation, research analytics va tich hop repository.

Pham vi quet source hien tai:

- Thu muc source: `src/main/java/com/fpt/swp/sealhackathonbe`
- So controller: 39 file `*Controller.java`
- So annotation mapping: 210 mapping annotation, bao gom ca endpoint alias va route phu
- Base version API chinh: `/api/v1`
- Mot so endpoint auth co alias cu `/auth` song song voi `/api/v1/auth`
- Tai lieu chi tong hop contract o muc route/chuc nang/quyen; body chi ghi ten DTO hoac tham so chinh de tra cuu nhanh.

## Quy uoc

| Cot | Y nghia |
| --- | --- |
| Method | HTTP method |
| Endpoint | Duong dan backend dang expose; neu co alias thi ghi chung mot dong |
| Request/Query chinh | DTO body hoac query/path param dang chu y |
| Quyen/doi tuong | Role hoac nhom user theo `@PreAuthorize`/service guard trong controller |
| Ghi chu chuc nang | Mo ta ngan bang tieng Viet |

> Luu y: quyen thuc te co the duoc kiem tra them trong service, dac biet cac luong team, submission repository, event organizer owner va consultation.

## Auth va tai khoan

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/auth/register`, `/api/v1/auth/register` | `RegisterRequest` | Public | Dang ky tai khoan local va khoi tao luong xac minh email. |
| POST | `/auth/resend-verification-email`, `/api/v1/auth/resend-verification-email` | `ResendVerificationEmailRequest` | Public | Gui lai email xac minh neu tai khoan can xac minh. |
| POST | `/auth/login`, `/api/v1/auth/login` | `LoginRequest` | Public | Dang nhap bang email/mat khau va nhan access/refresh token. |
| POST | `/auth/logout`, `/api/v1/auth/logout` | `LogoutRequest`, header `Authorization` | Dang nhap | Thu hoi refresh token cua phien hien tai. |
| POST | `/auth/refresh`, `/api/v1/auth/refresh` | `RefreshTokenRequest` | Public | Cap access token moi tu refresh token con hop le. |
| POST | `/auth/oauth2/exchange`, `/api/v1/auth/oauth2/exchange` | `OAuth2ExchangeRequest` | Public | Doi OAuth exchange code lay phien dang nhap. |
| GET | `/auth/verify-email`, `/api/v1/auth/verify-email` | query `token` | Public | Xac minh email va cap phien dang nhap ngay sau khi kich hoat. |
| POST | `/auth/forgot-password`, `/api/v1/auth/forgot-password` | `ForgotPasswordRequest` | Public | Gui link reset mat khau cho tai khoan local, khong tiet lo email ton tai hay khong. |
| POST | `/auth/reset-password`, `/api/v1/auth/reset-password` | `ResetPasswordRequest` | Public | Dat lai mat khau bang token reset. |
| POST | `/auth/google/link`, `/api/v1/auth/google/link` | `GoogleLinkRequest` | Dang nhap/link token | Gan dinh danh Google vao tai khoan hien co. |
| POST | `/auth/google/unlink`, `/api/v1/auth/google/unlink` | `GoogleUnlinkRequest` | Dang nhap | Go Google login khoi tai khoan hien tai, yeu cau mat khau local. |
| POST | `/auth/link/send-otp`, `/api/v1/auth/link/send-otp` | `LinkOtpSendRequest` | Link token | Gui OTP cho phien lien ket tai khoan. |
| POST | `/auth/link/verify-otp`, `/api/v1/auth/link/verify-otp` | `LinkOtpVerifyRequest` | Link token | Xac minh OTP cho phien lien ket tai khoan. |
| POST | `/auth/local/setup-password`, `/api/v1/auth/local/setup-password` | `SetupPasswordRequest` | Link token | Tao mat khau local cho tai khoan Google-only sau OTP. |

## Ho so ca nhan va quan ly user

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| GET | `/api/v1/me` | - | Dang nhap | Lay ho so user hien tai theo JWT. |
| PUT | `/api/v1/me` | `UpdateMyProfileRequest` | Dang nhap | Cap nhat ho so ca nhan: fullName, phone, universityName. |
| GET | `/api/v1/users/me` | - | Dang nhap | Lay thong tin user hien tai theo flow self-service. |
| PUT | `/api/v1/users/me/complete-profile` | `CompleteProfileRequest` | Dang nhap | Hoan thien profile cho user OAuth tam thoi, chon role sinh vien. |
| PUT | `/api/v1/users/me/profile` | `UpdateProfileRequest` | Dang nhap | Cap nhat profile cho user da ton tai. |
| GET | `/api/v1/users/me/link-candidates` | - | Dang nhap | Liet ke tai khoan local cung email co the lien ket. |
| POST | `/api/v1/users/me/link-local-account` | `LinkLocalAccountRequest` | Dang nhap | Lien ket OAuth account vao local account co san. |
| POST | `/api/v1/users/me/set-password` | `SetPasswordRequest` | Dang nhap | Dat mat khau local lan dau cho tai khoan OAuth-only. |
| GET | `/api/v1/users` | page, size, search, role, status, joinedFrom, joinedTo, sort | `ROLE_ORGANIZER` | Tim kiem/loc/paging danh sach user cho organizer. |
| GET | `/api/v1/users/facets` | filter query tuong tu search | `ROLE_ORGANIZER` | Lay facet count cho panel filter user. |
| GET | `/api/v1/users/{userId}` | path `userId` | `ROLE_ORGANIZER` | Xem chi tiet user theo ID. |
| POST | `/api/v1/users` | `CreateUserManagementRequest` | `ROLE_ORGANIZER` | Tao user moi tu man hinh quan ly. |
| PUT | `/api/v1/users/{userId}` | `UpdateUserManagementRequest` | `ROLE_ORGANIZER` | Cap nhat thong tin user trong quan ly. |
| PATCH | `/api/v1/users/{userId}/status` | `UpdateUserStatusRequest` | `ROLE_ORGANIZER` | Doi trang thai tai khoan user. |
| PATCH | `/api/v1/users/{userId}/role` | `UpdateUserRoleRequest` | `ROLE_ORGANIZER` | Doi role/user type cua user. |
| DELETE | `/api/v1/users/{userId}` | path `userId` | `ROLE_ORGANIZER` | Soft-delete/deactivate user. |
| DELETE | `/api/v1/users/hard-delete` | query `email`, `reason` | `ROLE_ORGANIZER` | Xoa vinh vien tat ca account theo email, dung nhu dev/admin tool. |
| GET | `/api/v1/public/judges/count` | - | Public | Dem tong so judge/expert active trong he thong. |

## Event, category va round

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| GET | `/api/v1/events` | - | Public | Lay danh sach event day du theo service hien tai. |
| GET | `/api/v1/public/events` | - | Public | Lay danh sach event public. |
| GET | `/api/v1/events/{id}` | path `id` | Public | Lay chi tiet event theo ID. |
| GET | `/api/v1/public/events/{id}` | path `id` | Public | Lay chi tiet event public theo ID. |
| GET | `/api/v1/event/{id}` | path `id` | Dang nhap/Public theo security config | Lay chi tiet event theo route legacy singular. |
| POST | `/api/v1/event` | `CreateEventRequest` | `ROLE_ORGANIZER` | Tao event moi. |
| PUT | `/api/v1/event/{id}` | `UpdateEventRequest` | `ROLE_ORGANIZER` | Cap nhat event. |
| PATCH | `/api/v1/event/status/{id}` | `UpdateEventStatusRequest` | `ROLE_ORGANIZER` | Doi trang thai event. |
| DELETE | `/api/v1/event/{id}` | path `id` | `ROLE_ORGANIZER` | Xoa event. |
| POST | `/api/v1/category/{eventId}` | `CreateCategoryRequest` | `ROLE_ORGANIZER` | Tao category trong event. |
| GET | `/api/v1/category/{id}` | path `id` | Dang nhap/Public theo security config | Lay chi tiet category. |
| PUT | `/api/v1/category/{id}` | `UpdateCategoryRequest` | `ROLE_ORGANIZER` | Cap nhat category. |
| DELETE | `/api/v1/category/{id}` | path `id` | `ROLE_ORGANIZER` | Xoa category. |
| GET | `/api/v1/categories/{eventId}`, `/api/v1/public/categories/events/{eventId}` | path `eventId` | Public | Lay danh sach category cua event. |
| POST | `/api/v1/category/expert/{categoryId}` | `AssignMentorsRequest` | `ROLE_ORGANIZER` | Gan expert/mentor vao category. |
| DELETE | `/api/v1/category/expert/{categoryId}/{mentorId}` | path params | `ROLE_ORGANIZER` | Go expert/mentor khoi category. |
| GET | `/api/v1/users/mentors` | - | Dang nhap/Public theo security config | Lay danh sach mentor/expert co the gan. |
| GET | `/api/v1/category/experts/{categoryId}` | path `categoryId` | Dang nhap/Public theo security config | Lay expert/mentor cua category. |
| GET | `/api/v1/{categoryId}` | path `categoryId` | `ROLE_ORGANIZER` | Lay mentor cua category theo route ngan trong `CategoryMentorController`. |
| POST | `/api/v1/round/{categoryId}` | `CreateRoundRequest` | `ROLE_ORGANIZER` | Tao round trong category. |
| GET | `/api/v1/round/{id}` | path `id` | Dang nhap/Public theo security config | Lay chi tiet round. |
| GET | `/api/v1/rounds/{categoryId}` | path `categoryId` | Dang nhap/Public theo security config | Lay danh sach round cua category. |
| PUT | `/api/v1/round/{id}` | `UpdateRoundRequest` | `ROLE_ORGANIZER` | Cap nhat round. |
| DELETE | `/api/v1/round/{id}` | path `id` | `ROLE_ORGANIZER` | Xoa round. |
| GET | `/api/v1/round/final/{categoryId}` | path `categoryId` | Dang nhap/Public theo security config | Lay round final cua category. |

## Tieu chi cham diem

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| GET | `/api/v1/criteria/templates` | - | Dang nhap/Public theo security config | Lay tat ca criterion template dang active. |
| GET | `/api/v1/criteria/template/{id}` | path `id` | Dang nhap/Public theo security config | Lay chi tiet criterion template. |
| POST | `/api/v1/criteria/template` | `CreateTemplateRequest` | `ROLE_ORGANIZER` | Tao criterion template. |
| PUT | `/api/v1/criteria/template/{id}` | `UpdateTemplateRequest` | `ROLE_ORGANIZER` | Cap nhat criterion template. |
| DELETE | `/api/v1/criteria/template/{id}` | path `id` | `ROLE_ORGANIZER` | Xoa criterion template. |
| POST | `/api/v1/event/criteria/import/{eventId}` | `ImportCriteriaToEventRequest` | `ROLE_ORGANIZER` | Import tieu chi tu template vao event. |
| GET | `/api/v1/event/criteria/{eventId}` | path `eventId` | Dang nhap/Public theo security config | Lay tieu chi cap event. |
| PUT | `/api/v1/event/criteria/{id}` | `UpdateEventCriterionRequest` | `ROLE_ORGANIZER` | Cap nhat tieu chi cap event. |
| DELETE | `/api/v1/event/criteria/{id}` | path `id` | `ROLE_ORGANIZER` | Xoa tieu chi cap event. |
| GET | `/api/v1/rounds/criterion/{id}` | path `id` | Dang nhap/Public theo security config | Lay chi tiet tieu chi cua round. |
| GET | `/api/v1/rounds/criteria/{roundId}` | path `roundId` | Dang nhap/Public theo security config | Lay danh sach tieu chi cua round. |
| POST | `/api/v1/rounds/criteria/import/{roundId}` | `ImportCriteriaFromEventRequest` | `ROLE_ORGANIZER` | Import tieu chi tu event vao round. |
| POST | `/api/v1/rounds/criterion/{roundId}` | `CreateSpecificCriterionRequest` | `ROLE_ORGANIZER` | Tao tieu chi rieng cho round. |
| PUT | `/api/v1/rounds/criterion/import/{id}` | `UpdateImportedCriterionRequest` | `ROLE_ORGANIZER` | Cap nhat tieu chi duoc import. |
| PUT | `/api/v1/rounds/criterion/{id}` | `UpdateSpecificCriterionRequest` | `ROLE_ORGANIZER` | Cap nhat tieu chi rieng cua round. |
| DELETE | `/api/v1/rounds/criterion/{id}` | path `id` | `ROLE_ORGANIZER` | Xoa tieu chi round. |

## Phan cong judge

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/round/judges/{roundId}` | `AssignJudgesRequest` | `ROLE_ORGANIZER` | Phan cong judge cho round. |
| GET | `/api/v1/round/judges/{roundId}` | path `roundId` | Dang nhap/Public theo security config | Lay danh sach judge cua round. |
| GET | `/api/v1/judge/rounds/{judgeId}` | path `judgeId` | Organizer hoac chinh judge | Lay cac round duoc phan cong cho judge. |
| PATCH | `/api/v1/round/judge/{id}` | query `force` | `ROLE_ORGANIZER` | Disable judge assignment, co tuy chon force. |
| GET | `/api/v1/users/judges` | - | Dang nhap/Public theo security config | Lay danh sach judge. |
| GET | `/api/v1/public/test-judges` | - | Public | Route test lay danh sach judge. |

## Team va dang ky tham gia event

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/teams` | `CreateTeamRequest` | Dang nhap | Tao team, user hien tai thanh leader. |
| GET | `/api/v1/teams/{teamId}` | path `teamId` | Dang nhap | Lay chi tiet team va member active. |
| GET | `/api/v1/events/{eventId}/teams` | path `eventId` | Dang nhap | Lay team trong event. |
| GET | `/api/v1/admin/events/{eventId}/teams/eligibility-review` | path `eventId` | `ROLE_ORGANIZER` | Organizer review eligibility cua team trong event. |
| POST | `/api/v1/admin/teams/{teamId}/eligibility-decision` | `EligibilityDecisionRequest` | `ROLE_ORGANIZER` | Duyet hoac tu choi eligibility cua team, dong bo participant pending. |
| POST | `/api/v1/teams/{teamId}/register-event` | path `teamId` | Leader team | Dang ky ca team vao event cua team. |
| DELETE | `/api/v1/teams/{teamId}/register-event` | path `teamId` | Leader team | Rut dang ky team khi toan bo con pending. |
| GET | `/api/v1/teams/{teamId}/members/{userId}` | path params | Member active cung team | Xem chi tiet thanh vien trong team. |
| POST | `/api/v1/teams/{teamId}/join` | path `teamId` | Dang nhap | Gui request xin vao team. |
| GET | `/api/v1/teams/{teamId}/requests` | path `teamId` | Leader team | Xem request pending xin vao team. |
| PUT | `/api/v1/teams/requests/{requestId}` | `HandleJoinRequest` | Leader team | Duyet hoac tu choi request xin vao team. |
| DELETE | `/api/v1/teams/requests/{requestId}` | path `requestId` | Chu request | Huy request pending cua chinh minh. |
| GET | `/api/v1/teams/mine` | - | Dang nhap | Lay cac team ma user hien tai tham gia. |
| GET | `/api/v1/teams/requests/mine` | - | Dang nhap | Lay cac request xin vao team cua user hien tai. |
| POST | `/api/v1/teams/{teamId}/disband` | path `teamId` | Leader team | Giai tan team dang forming. |
| DELETE | `/api/v1/teams/{teamId}/members/{userId}` | `RemoveTeamMemberRequest` optional | Leader/member | Kick member hoac tu roi team. |
| PUT | `/api/v1/teams/{teamId}/leader` | `TransferTeamLeadershipRequest` | Leader team | Chuyen quyen leader cho member active khac. |
| POST | `/api/v1/admin/teams/{teamId}/disqualify` | `DisqualifyTeamRequest` | `ROLE_ORGANIZER` | Disqualify team va luu ly do. |
| GET | `/api/v1/public/teams/count` | - | Public | Dem tong so team. |
| POST | `/api/v1/events/{eventId}/participants/register`, `/api/v1/events/{eventId}/register` | path `eventId` | Dang nhap | Dang ky ca nhan tham gia event. |
| GET | `/api/v1/events/{eventId}/participants/me`, `/api/v1/events/{eventId}/registration-status` | path `eventId` | Dang nhap | Xem trang thai dang ky event cua user hien tai. |
| GET | `/api/v1/users/me/event-participations`, `/api/v1/event-participants/me` | - | Dang nhap | Xem cac event participation cua user hien tai. |
| GET | `/api/v1/organizer/event-participants` | filter, page, sort | `ROLE_ORGANIZER` | Tim kiem participant theo event/category/status/keyword. |
| GET | `/api/v1/events/{eventId}/participants` | filter, page, sort | `ROLE_ORGANIZER` | Liet ke participant cua mot event. |
| PATCH | `/api/v1/organizer/event-participants/{id}/status`, `/api/v1/event-participants/{id}/status` | `EventParticipantStatusUpdateRequest` | `ROLE_ORGANIZER` | Cap nhat status cua mot participant. |
| PATCH | `/api/v1/organizer/event-participants/status`, `/api/v1/event-participants/status` | `EventParticipantBulkStatusUpdateRequest` | `ROLE_ORGANIZER` | Cap nhat status hang loat participant. |

## Submission va repository metadata

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/submissions` | `CreateSubmissionRequest` | Dang nhap/team member theo service | Nop hoac cap nhat bai lam cua team trong round. |
| POST | `/api/v1/admin/calibration-sample-submissions` | `CreateSampleSubmissionRequest` | `ROLE_ORGANIZER` | Tao/cap nhat sample submission cho calibration round. |
| GET | `/api/v1/teams/{teamId}/rounds/{roundId}/submission` | path params | Theo quyen service | Lay submission hien tai cua team trong round. |
| GET | `/api/v1/teams/{teamId}/rounds/{roundId}/submission/history` | path params | Theo quyen service | Lay lich su cac version submission cua team trong round. |
| GET | `/api/v1/admin/rounds/{roundId}/submissions` | path `roundId` | Organizer/judge/expert | Lay tat ca submission trong round. |
| GET | `/api/v1/admin/submissions/{submissionId}/history` | path `submissionId` | Organizer/judge/expert | Lay lich su version cua mot submission. |
| GET | `/api/v1/admin/rounds/{roundId}/unreview-submissions` | path `roundId` | Organizer/judge/expert | Lay submission chua duoc cham diem. |
| GET | `/api/v1/admin/events/{eventId}/submissions` | path `eventId` | `ROLE_ORGANIZER` | Lay submission cua tat ca team trong event. |
| POST | `/api/v1/admin/submissions/{submissionId}/disqualify` | `DisqualifySubmissionRequest` | `ROLE_ORGANIZER` | Disqualify submission va luu ly do. |
| POST | `/api/v1/admin/submissions/{submissionId}/approve` | body map `approve` | `ROLE_ORGANIZER` | Duyet hoac huy duyet diem cua submission. |
| POST | `/api/v1/admin/submissions/{submissionId}/reject-score` | body map `reason` | `ROLE_ORGANIZER` | Tu choi diem va yeu cau judge cham lai. |
| GET | `/api/v1/public/submissions/count` | - | Public | Dem tong so submission. |
| POST | `/api/v1/submissions/repository/validate` | `ValidateRepositoryRequest` | Dang nhap | Validate repo URL va preview metadata, chua persist. |
| GET | `/api/v1/submissions/{submissionId}/repository` | path `submissionId` | Team member/judge/organizer theo service | Lay metadata repository cua submission. |
| POST | `/api/v1/submissions/{submissionId}/repository/sync` | path `submissionId` | Team member/organizer theo service | Dong bo lai metadata repository cua submission. |
| GET | `/api/v1/events/{eventId}/submission-repositories` | path `eventId` | Organizer owner theo service | Organizer xem tong quan repo cua moi submission trong event. |
| GET | `/api/v1/events/{eventId}/submission-repositories/export` | path `eventId` | Organizer owner theo service | Export CSV tong quan repo cua event. |

## Tich hop repository legacy cap event

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/events/{eventId}/integrations/repository/test` | `ConnectIntegrationRequest` | Creator event theo service | Test ket noi repository bang URL/token, chua luu. |
| POST | `/api/v1/events/{eventId}/integrations/repository/connect` | `ConnectIntegrationRequest` | Creator event theo service | Ket noi repository legacy cap event va dong bo metadata. |
| GET | `/api/v1/events/{eventId}/integrations/repository` | path `eventId` | Creator event theo service | Lay danh sach repository integration cua event. |
| POST | `/api/v1/events/{eventId}/integrations/repository/{repositoryId}/sync` | path params | Creator event theo service | Trigger sync thu cong repository legacy. |
| DELETE | `/api/v1/events/{eventId}/integrations/repository/{integrationId}` | path params | Creator event theo service | Ngat ket noi integration legacy. |
| GET | `/api/v1/events/{eventId}/integrations/repository/{repositoryId}/issues` | page, size | Creator event theo service | Lay issue da sync cua repository legacy. |

## Judging, ranking va appeal

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/judging` | `List<ScoreSubmissionDTO>` | Internal Judge/Guest Judge/Expert | Ghi diem cho submission. |
| PATCH | `/api/v1/judging` | `List<UpdateScoreSubmissionDTO>` | Internal Judge/Guest Judge/Expert | Cap nhat diem da cham. |
| DELETE | `/api/v1/judging/submission/{submissionId}` | query `reason` | Internal Judge/Guest Judge/Expert | Xoa diem cua judge cho submission. |
| POST | `/api/v1/judging/batch-scores` | `BatchScoreRequestDTO` | Organizer/Admin | Lay diem cua nhieu submission. |
| GET | `/api/v1/judging/submission/{submissionId}` | path `submissionId` | Organizer/judge/expert | Lay diem cua mot submission. |
| GET | `/api/v1/judging/team-submission/{submissionId}/published` | path `submissionId` | FPT/External Student | Lay diem da publish cho submission cua team. |
| GET | `/api/v1/judging/judge/{judgeUserId}` | path `judgeUserId` | Organizer hoac chinh judge | Lay lich su cham diem cua judge. |
| GET | `/api/v1/judging/audit-logs/event/{eventId}` | path `eventId` | `ROLE_ORGANIZER` | Lay audit log thay doi diem trong event. |
| GET | `/api/v1/judging/events/{eventId}/calibration-metrics` | eventId/roundId/categoryId | Organizer/judge/expert | Lay metric reliability/calibration theo event/round/category. |
| POST | `/api/v1/admin/events/{id}/compute-rankings` | path `id` | `ROLE_ORGANIZER` | Tinh ranking final cho event. |
| POST | `/api/v1/admin/rounds/{roundId}/compute-rankings` | query `categoryId` | `ROLE_ORGANIZER` | Tinh ranking cho round/category. |
| POST | `/api/v1/admin/rounds/{roundId}/publish-rankings` | query `categoryId` | `ROLE_ORGANIZER` | Publish ranking cua round/category. |
| POST | `/api/v1/admin/rounds/{roundId}/approve-rankings` | query `categoryId` | `ROLE_ORGANIZER` | Approve/lock ranking cua round/category. |
| POST | `/api/v1/admin/events/{eventId}/publish-rankings` | query optional `categoryId` | `ROLE_ORGANIZER` | Publish final ranking cua event/category. |
| POST | `/api/v1/admin/categories/{categoryId}/compute-rankings` | path `categoryId` | `ROLE_ORGANIZER` | Tinh final ranking cua category. |
| POST | `/api/v1/admin/categories/{categoryId}/publish-rankings` | path `categoryId` | `ROLE_ORGANIZER` | Publish final ranking cua category. |
| POST | `/api/v1/admin/categories/{categoryId}/approve-rankings` | path `categoryId` | `ROLE_ORGANIZER` | Approve/lock final ranking cua category. |
| GET | `/api/v1/admin/rounds/{roundId}/rankings` | query `categoryId` | `ROLE_ORGANIZER` | Lay ranking hien co cua round/category. |
| GET | `/api/v1/admin/events/{eventId}/categories/{categoryId}/rankings` | path params | `ROLE_ORGANIZER` | Lay ranking final cua category trong event. |
| GET | `/api/v1/admin/events/{eventId}/rankings` | path `eventId` | `ROLE_ORGANIZER` | Lay ranking final cua event. |
| GET | `/api/v1/public/leaderboard/{eventId}/{categoryId}` | path params | Public | Lay leaderboard public cua event/category. |
| GET | `/api/v1/public/leaderboard/rounds/{roundId}/{categoryId}` | path params | Public | Lay leaderboard public cua round/category. |
| POST | `/api/v1/appeals` | `AppealRequestDTO` | FPT/External Student | Tao appeal cho diem/ket qua. |
| PATCH | `/api/v1/appeals/{appealId}/resolve` | `AppealResolutionDTO` | Organizer | Xu ly/resolution appeal. |
| GET | `/api/v1/appeals/team/{teamId}` | path `teamId` | FPT/External Student | Lay appeal cua team. |
| GET | `/api/v1/appeals/event/{eventId}` | path `eventId` | Organizer | Lay appeal cua event. |

## Award va certificate

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/awards/grandAwardToATeam` | `AwardRequest` | `ROLE_ORGANIZER` | Trao giai thuong cho team trong event. |
| GET | `/api/v1/awards/{id}` | path `id` | Dang nhap/Public theo security config | Lay chi tiet award. |
| POST | `/api/v1/awards/templates/categories/{categoryId}/award-patterns` | `AwardPatternRequest` | `ROLE_ORGANIZER` | Tao/cap nhat mau giai thuong theo rank trong category. |
| GET | `/api/v1/awards/categories/{categoryId}/award-patterns` | path `categoryId` | Dang nhap/Public theo security config | Lay mau giai thuong active cua category. |
| GET | `/api/v1/awards/categories/{categoryId}/rankings/top` | roundId, limit | Dang nhap/Public theo security config | Lay top team tu ranking de de xuat trao giai. |
| POST | `/api/v1/awards/categories/{categoryId}/auto-grant-top` | roundId, limit | `ROLE_ORGANIZER` | Tu dong trao giai theo award pattern va ranking top. |
| GET | `/api/v1/awards/events/{eventId}`, `/api/v1/public/awards/events/{eventId}` | path `eventId` | Public | Lay award theo event. |
| GET | `/api/v1/awards/events/{eventId}/total-prize`, `/api/v1/public/awards/events/{eventId}/total-prize` | path `eventId` | Public | Lay tong tien thuong cua mot event. |
| GET | `/api/v1/awards/events/total-prize`, `/api/v1/public/awards/events/total-prize` | - | Public | Lay tong tien thuong cua toan he thong. |
| GET | `/api/v1/public/hall-of-fame` | - | Public | Lay du lieu hall of fame. |
| GET | `/api/v1/certificates/download/{awardId}` | path `awardId` | Student trong team dat giai theo service | Download certificate PDF cho award da publish. |

## Consultation va milestone

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/categories/{categoryId}/experts/{expertId}` | path params | Organizer | Gan expert vao category trong consultation system. |
| DELETE | `/api/v1/categories/{categoryId}/experts/{expertId}` | path params | Organizer | Go expert khoi category. |
| GET | `/api/v1/categories/{categoryId}/experts` | path `categoryId` | Organizer/Mentor/Expert/Student | Lay expert cua category. |
| GET | `/api/v1/expert/categories` | - | Mentor/Expert | Lay category duoc gan cho expert hien tai. |
| GET | `/api/v1/expert/categories/{categoryId}/teams` | path `categoryId` | Mentor/Expert | Lay team trong category ma expert phu trach. |
| GET | `/api/v1/expert/consultation-requests` | categoryId, teamId, status, priority, page, size | Mentor/Expert | Lay request consultation cua expert. |
| PUT | `/api/v1/expert/consultation-requests/{requestId}/accept` | path `requestId` | Mentor/Expert | Chap nhan consultation request. |
| PUT | `/api/v1/expert/consultation-requests/{requestId}/reject` | `RejectRequest` | Mentor/Expert | Tu choi consultation request va ghi ly do. |
| PUT | `/api/v1/expert/consultation-requests/{requestId}/in-progress` | path `requestId` | Mentor/Expert | Chuyen request sang dang xu ly. |
| PUT | `/api/v1/expert/consultation-requests/{requestId}/resolve` | path `requestId` | Mentor/Expert | Danh dau request da giai quyet. |
| GET | `/api/v1/expert/consultation-requests/{requestId}/note` | path `requestId` | Mentor/Expert | Lay ghi chu rieng cua mentor cho request. |
| PUT | `/api/v1/expert/consultation-requests/{requestId}/note` | `TeamMentorNoteRequest` | Mentor/Expert | Cap nhat ghi chu rieng cua mentor. |
| GET | `/api/v1/teams/my-experts` | - | FPT/External Student | Lay expert duoc gan cho team cua user hien tai. |
| GET | `/api/v1/consultation-requests/{requestId}/mentor-notes` | path `requestId` | FPT/External Student | Student xem mentor notes cua request cua team minh. |
| POST | `/api/v1/consultation-requests` | `CreateConsultationRequestRequest` | FPT/External Student | Tao request tu van cho team. |
| GET | `/api/v1/consultation-requests/my-team` | status, page, size | FPT/External Student | Lay consultation request cua team minh. |
| PUT | `/api/v1/consultation-requests/{requestId}/cancel` | path `requestId` | FPT/External Student | Huy consultation request. |
| GET | `/api/v1/consultation-requests/{requestId}` | path `requestId` | Mentor/Expert/Student/Organizer | Lay chi tiet consultation request. |
| GET | `/api/v1/consultation-requests/{requestId}/messages` | path `requestId` | Mentor/Expert/Student/Organizer | Lay tin nhan trong request. |
| POST | `/api/v1/consultation-requests/{requestId}/messages` | `MessageRequest` | Mentor/Expert/Student | Gui tin nhan trong request. |
| GET | `/api/v1/consultation-requests/{requestId}/milestones` | path `requestId` | Mentor/Expert/Organizer/Student | Lay milestone cua consultation request. |
| POST | `/api/v1/consultation-requests/{requestId}/milestones` | `CreateMilestoneRequest` | Mentor/Expert | Tao milestone moi. |
| PATCH | `/api/v1/consultation-requests/{requestId}/milestones/{milestoneId}/toggle` | path params | Mentor/Expert/Student | Toggle milestone done/undone. |
| DELETE | `/api/v1/consultation-requests/{requestId}/milestones/{milestoneId}` | path params | Mentor/Expert | Xoa milestone. |

## Notification

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| GET | `/api/v1/notifications/getMyNotifications` | page, size | Dang nhap | Lay notification cua user hien tai co paging. |
| GET | `/api/v1/notifications/unread-count` | - | Dang nhap | Dem notification chua doc cua user hien tai. |
| GET | `/api/v1/notifications/stream` | SSE | Dang nhap | Mo Server-Sent Events stream nhan notification realtime. |
| POST | `/api/v1/notifications/sendNotificationToUser` | `CreateNotificationRequest` | `ROLE_ORGANIZER` | Gui notification truc tiep cho mot user. |
| POST | `/api/v1/notifications/sendNotificationToEmail` | `CreateNotificationByEmailRequest` | `ROLE_ORGANIZER` | Gui in-app notification va email theo email. |
| POST | `/api/v1/notifications/sendBroadcastNotification` | `BroadcastNotificationRequest` | `ROLE_ORGANIZER` | Gui broadcast notification cho nhieu user. |
| PATCH | `/api/v1/notifications/{notificationId}/read` | path `notificationId` | Dang nhap | Danh dau mot notification la da doc. |
| PATCH | `/api/v1/notifications/read-all` | - | Dang nhap | Danh dau tat ca notification la da doc. |
| DELETE | `/api/v1/notifications/deleteNotification/{notificationId}` | path `notificationId` | Dang nhap | Xoa notification thuoc user hien tai. |

## Research, download, AI va settings

| Method | Endpoint | Request/Query chinh | Quyen/doi tuong | Ghi chu chuc nang |
| --- | --- | --- | --- | --- |
| GET | `/api/v1/student-downloads/rounds/{roundId}/problem` | query `type=csv/zip` | Student authenticated theo service | Download de bai round theo dinh dang yeu cau. |
| GET | `/api/v1/student-downloads/rounds/{roundId}/problem-csv` | path `roundId` | Student authenticated theo service | Download de bai round dang CSV UTF-8. |
| GET | `/api/v1/student-downloads/rounds/{roundId}/problem-zip` | path `roundId` | Student authenticated theo service | Download de bai round dang ZIP. |
| GET | `/api/v1/research/calibration-metrics`, `/api/v1/research/reliability-metrics` | eventId, categoryId, roundId | Organizer/judge/expert | Lay reliability/calibration metrics cho judge. |
| GET | `/api/v1/research/events/{eventId}/export` | roundId, categoryId, bucketSize, type | `ROLE_ORGANIZER` | Export CSV du lieu research/dashboard/variance/distribution/reliability. |
| GET | `/api/v1/research/calibration/matrix/{roundId}` | path `roundId` | Organizer/judge/expert | Lay consensus matrix cua round. |
| GET | `/api/v1/research/calibration/export/{roundId}` | path `roundId` | Organizer/judge/expert | Export calibration CSV dang wide-format. |
| POST | `/api/v1/ai-knowledge` | `CreateAiKnowledgeRequest` | Mentor/Expert/Organizer/Admin | Tao knowledge item cho AI knowledge base. |
| GET | `/api/v1/ai-knowledge/event/{eventId}` | path `eventId` | Mentor/Expert/Organizer/Admin | Lay AI knowledge theo event. |
| DELETE | `/api/v1/ai-knowledge/{id}` | path `id` | Mentor/Expert/Organizer/Admin | Xoa AI knowledge item. |
| GET | `/api/v1/settings` | - | Organizer | Lay system settings hien tai. |
| PUT | `/api/v1/settings` | `SystemSettingsRequest` | Organizer | Cap nhat system settings. |
