package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.client.GitRepositoryMetadataClient;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryActivity;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadataFetchResult;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.EventSubmissionRepositoryItemResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.SubmissionRepositoryResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncStatus;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.SubmissionRepositoryEntity;
import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataException;
import com.fpt.swp.sealhackathonbe.integration.repository.mapper.SubmissionRepositoryMapper;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.SubmissionRepositoryEntityRepository;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionRepositoryService {

    // Mot phien RUNNING giu lock qua lau (request truoc chet giua chung) thi cho phep
    // request sau lay lai lock. 2 phut > connect(5s)+read(10s) timeout cua GitHub client.
    private static final int SYNC_STALE_MINUTES = 2;

    // Cooldown giua 2 lan resync-THAT (goi GitHub) tren cung submission. Trong khoang nay,
    // resync tra ve snapshot hien tai (khong goi GitHub) de chong spam nut Resync + bao ve
    // rate-limit GitHub (dung chung theo IP server).
    private static final int RESYNC_COOLDOWN_SECONDS = 30;

    private final SubmissionRepositoryEntityRepository submissionRepositoryRepository;
    private final SubmissionsRepository submissionsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamsRepository teamsRepository;
    private final EventRepository eventRepository;
    private final RoundRepository roundRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final UserRepository userRepository;
    private final List<GitRepositoryMetadataClient> gitRepositoryMetadataClients;
    private final SubmissionRepositoryMapper submissionRepositoryMapper;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_ACCESS_DENIED, "User not authenticated");
        }
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_ACCESS_DENIED, "User not found");
        }
        return user;
    }

    private GitRepositoryMetadataClient selectClient(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Repository URL cannot be empty");
        }
        return gitRepositoryMetadataClients.stream()
                .filter(client -> client.supports(repositoryUrl))
                .findFirst()
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Unsupported repository URL provider"));
    }

    /**
     * Preview cho Team truoc khi submit: goi GitHub nhung KHONG persist.
     * Loi (URL sai, repo private, 404...) duoc nem nguyen RepositoryMetadataException
     * de GlobalExceptionHandler map sang HTTP status + error code an toan cho FE.
     */
    public SubmissionRepositoryResponse validateRepositoryUrl(String repositoryUrl) {
        GitRepositoryMetadataClient client = selectClient(repositoryUrl);
        RepositoryMetadata metadata = client.fetchPublicMetadata(repositoryUrl);
        return mapMetadataToResponse(metadata);
    }

    /**
     * Goi GitHub NGOAI transaction DB (khong duoc goi tu trong @Transactional).
     * Khong nem exception: that bai duoc dong goi thanh FetchResult.failure de
     * caller van persist duoc trang thai FAILED + error code an toan.
     */
    public RepositoryMetadataFetchResult fetchMetadataOutsideTx(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            return null;
        }
        try {
            GitRepositoryMetadataClient client = selectClient(repositoryUrl);
            RepositoryMetadata core = client.fetchPublicMetadata(repositoryUrl);
            // Activity (languages/contributors/commits) la best-effort — khong bao gio nem loi,
            // fail chi khien field null. Gop vao metadata core de persist mot the.
            RepositoryActivity activity;
            try {
                activity = client.fetchActivity(repositoryUrl);
            } catch (Exception e) {
                log.debug("fetchActivity unexpected failure for {}: {}", repositoryUrl, e.getMessage());
                activity = RepositoryActivity.builder().build();
            }
            RepositoryMetadata full = core.toBuilder()
                    .languagesJson(activity.getLanguagesJson())
                    .contributorCount(activity.getContributorCount())
                    .topContributorsJson(activity.getTopContributorsJson())
                    .commitCount(activity.getCommitCount())
                    .lastCommitSha(activity.getLastCommitSha())
                    .build();
            return RepositoryMetadataFetchResult.success(full);
        } catch (RepositoryMetadataException rme) {
            log.warn("Repository metadata fetch failed for URL {}: {} - {}", repositoryUrl, rme.getErrorCode(), rme.getMessage());
            return RepositoryMetadataFetchResult.failure(
                    rme.getErrorCode() != null ? rme.getErrorCode().name() : "GITHUB_UPSTREAM_ERROR",
                    rme.getMessage());
        } catch (RepositoryIntegrationException rie) {
            log.warn("Repository metadata fetch rejected for URL {}: {}", repositoryUrl, rie.getMessage());
            return RepositoryMetadataFetchResult.failure(rie.getErrorCode().name(), rie.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected repository metadata fetch failure for URL {}", repositoryUrl, e);
            return RepositoryMetadataFetchResult.failure("GITHUB_UPSTREAM_ERROR", "Unexpected error while fetching repository metadata");
        }
    }

    /**
     * Persist metadata trong MOT transaction ngan: cap nhat/tao ban ghi SubmissionRepositories
     * (unique theo SubmissionID) va dong bo nguoc Submissions.RepositoryURL de tuong thich code cu.
     * GitHub PHAI duoc goi truoc do bang fetchMetadataOutsideTx.
     */
    @Transactional
    public SubmissionRepositoryResponse saveOrUpdateSubmissionRepository(UUID submissionId, String repositoryUrl, RepositoryMetadataFetchResult fetchResult) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            return null;
        }

        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found"));

        SubmissionRepositoryEntity repoEntity = submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId)
                .orElseGet(() -> {
                    SubmissionRepositoryEntity newEntity = new SubmissionRepositoryEntity();
                    newEntity.setSubmission(submission);
                    return newEntity;
                });

        repoEntity.setRepositoryUrl(repositoryUrl);
        submissionRepositoryMapper.applyFetchResult(fetchResult, repoEntity);

        // Metadata thanh cong co the tra ve URL da chuan hoa (bo .git, bo "/" cuoi):
        // dung URL chuan hoa lam nguon dong bo cho ca hai bang.
        String canonicalUrl = repoEntity.getRepositoryUrl() != null ? repoEntity.getRepositoryUrl() : repositoryUrl;

        repoEntity = submissionRepositoryRepository.save(repoEntity);

        if (!canonicalUrl.equals(submission.getRepositoryUrl())) {
            submission.setRepositoryUrl(canonicalUrl);
            submissionsRepository.save(submission);
        }

        return mapToResponse(repoEntity);
    }

    /**
     * Xac dinh event chua submission: team.eventId cho submission cua team,
     * round -> category -> event cho sample submission (teamId null).
     * Dung query theo ID de an toan khi duoc goi ngoai transaction (khong cham lazy proxy).
     */
    private UUID resolveEventId(Submissions submission) {
        if (submission.getTeamId() != null) {
            return teamsRepository.findById(submission.getTeamId())
                    .map(Teams::getEventId)
                    .orElse(null);
        }
        return roundRepository.findEventIdByRoundId(submission.getRoundId()).orElse(null);
    }

    private boolean isEventOrganizer(UUID eventId, UUID userId) {
        return eventId != null && eventRepository.existsByEventIdAndCreatedBy_UserId(eventId, userId);
    }

    private boolean isActiveTeamMember(Submissions submission, UUID userId) {
        return submission.getTeamId() != null
                && teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(submission.getTeamId(), userId).isPresent();
    }

    /**
     * Xem metadata: thanh vien team cua submission, Organizer cua DUNG event chua submission,
     * hoac Judge duoc phan cong vao round cua submission. Moi vai tro khac bi 403.
     */
    void authorizeView(Submissions submission, UUID currentUserId) {
        if (isActiveTeamMember(submission, currentUserId)) {
            return;
        }
        if (isEventOrganizer(resolveEventId(submission), currentUserId)) {
            return;
        }
        boolean isAssignedJudge = roundJudgeRepository
                .findByJudge_UserIdAndRound_RoundId(currentUserId, submission.getRoundId())
                .isPresent();
        if (isAssignedJudge) {
            return;
        }
        throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_ACCESS_DENIED, "Access denied to submission repository metadata");
    }

    /**
     * Resync: thanh vien team, bat ky tai khoan co ROLE_ORGANIZER (theo dev - Organizer
     * hien khong duoc gioi han theo tung event), HOAC judge duoc phan cong vao round.
     * Judge duoc phep resync de tu nap ban MOI NHAT cua repo khi cham (chi lam moi metadata
     * public tu GitHub — khong sua repo, rui ro thap).
     */
    void authorizeResync(Submissions submission, UUID currentUserId) {
        if (isActiveTeamMember(submission, currentUserId)) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasOrganizerRole = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));
        if (hasOrganizerRole) {
            return;
        }
        boolean isAssignedJudge = roundJudgeRepository
                .findByJudge_UserIdAndRound_RoundId(currentUserId, submission.getRoundId())
                .isPresent();
        if (isAssignedJudge) {
            return;
        }
        throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_MODIFICATION_NOT_ALLOWED, "Only team members, organizers, or an assigned judge may resynchronize repository metadata");
    }

    /**
     * README (raw markdown) — lazy, goi GitHub khi nguoi xem mo. Cung pham vi quyen nhu xem
     * metadata (team member / organizer / assigned judge). KHONG @Transactional vi co goi mang.
     */
    public String getReadme(UUID submissionId) {
        User currentUser = getCurrentUser();
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found"));
        authorizeView(submission, currentUser.getUserId());

        String repoUrl = submission.getRepositoryUrl();
        if (repoUrl == null || repoUrl.trim().isEmpty()) {
            return null;
        }
        return selectClient(repoUrl).fetchReadme(repoUrl);
    }

    @Transactional(readOnly = true)
    public SubmissionRepositoryResponse getSubmissionRepository(UUID submissionId) {
        User currentUser = getCurrentUser();
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found"));

        authorizeView(submission, currentUser.getUserId());

        return submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Resync thu cong. Khong co @Transactional o day la CHU DICH:
     * buoc goi GitHub (cham, co the 15s) phai nam ngoai transaction DB;
     * chi markSyncRunning va saveOrUpdate... mo transaction ngan.
     */
    public SubmissionRepositoryResponse syncSubmissionRepository(UUID submissionId) {
        User currentUser = getCurrentUser();
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found"));

        authorizeResync(submission, currentUser.getUserId());

        String repoUrl = submission.getRepositoryUrl();
        if (repoUrl == null || repoUrl.trim().isEmpty()) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_NOT_FOUND, "Submission has no repository URL attached");
        }

        Optional<SubmissionRepositoryEntity> existing = submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId);

        // Cooldown chong spam: vua dong bo trong RESYNC_COOLDOWN_SECONDS thi tra snapshot hien tai,
        // KHONG goi GitHub. Judge co bam lien tuc thi cung chi doc DB, khong dot rate-limit GitHub.
        if (existing.isPresent()) {
            LocalDateTime lastSync = existing.get().getLastSynchronizedAt();
            if (lastSync != null && lastSync.isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(RESYNC_COOLDOWN_SECONDS))) {
                return mapToResponse(existing.get());
            }
        }

        // Chong resync dong thoi: chi ap dung khi da co ban ghi metadata (lan dau sync thi
        // unique constraint tren SubmissionID la lop bao ve cuoi cung chong ghi trung).
        boolean hasExistingRecord = existing.isPresent();
        if (hasExistingRecord) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            int locked = submissionRepositoryRepository.markSyncRunning(
                    submissionId, now, now.minusMinutes(SYNC_STALE_MINUTES), RepositorySyncStatus.RUNNING);
            if (locked == 0) {
                throw new RepositoryIntegrationException(
                        RepositoryIntegrationException.ErrorCode.REPOSITORY_SYNC_ALREADY_RUNNING,
                        "A repository synchronization is already running for this submission");
            }
        }

        try {
            RepositoryMetadataFetchResult fetched = fetchMetadataOutsideTx(repoUrl);
            return saveOrUpdateSubmissionRepository(submissionId, repoUrl, fetched);
        } catch (RuntimeException e) {
            // Tra lock ve FAILED de submission khong ket o RUNNING den het stale timeout.
            if (hasExistingRecord) {
                try {
                    submissionRepositoryRepository.failRunningSync(
                            submissionId, "GITHUB_UPSTREAM_ERROR", LocalDateTime.now(ZoneOffset.UTC),
                            RepositorySyncStatus.RUNNING, RepositorySyncStatus.FAILED);
                } catch (Exception releaseError) {
                    log.warn("Failed to release RUNNING sync lock for submission {}", submissionId, releaseError);
                }
            }
            throw e;
        }
    }

    /**
     * Organizer overview: toan bo repository cua cac submission trong event.
     * Cho phep bat ky tai khoan co ROLE_ORGANIZER xem, khong gioi han chi creator.
     * Doc trong mot transaction readOnly, batch fetch de tranh N+1.
     */
    @Transactional(readOnly = true)
    public List<EventSubmissionRepositoryItemResponse> getEventSubmissionRepositories(UUID eventId) {
        getCurrentUser(); // Xac thuc user dang nhap hop le
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasOrganizerRole = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));
        if (!hasOrganizerRole) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_ACCESS_DENIED, "Only organizers may view submission repositories of this event");
        }

        // Bao gồm cả sample submission (bài mẫu calibration, TeamID null) — Organizer cần thấy.
        List<Submissions> submissions = submissionsRepository.findByEventIdIncludingSamples(eventId);
        if (submissions.isEmpty()) {
            return List.of();
        }

        List<UUID> submissionIds = submissions.stream().map(Submissions::getSubmissionId).toList();
        Map<UUID, SubmissionRepositoryEntity> reposBySubmission = submissionRepositoryRepository
                .findBySubmission_SubmissionIdIn(submissionIds).stream()
                .collect(Collectors.toMap(r -> r.getSubmission().getSubmissionId(), Function.identity()));

        List<UUID> teamIds = submissions.stream().map(Submissions::getTeamId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<UUID, Teams> teamsById = teamsRepository.findAllById(teamIds).stream()
                .collect(Collectors.toMap(Teams::getTeamId, Function.identity()));

        List<UUID> roundIds = submissions.stream().map(Submissions::getRoundId).distinct().toList();
        Map<UUID, Round> roundsById = roundRepository.findAllById(roundIds).stream()
                .collect(Collectors.toMap(Round::getRoundId, Function.identity()));

        return submissions.stream()
                .map(submission -> toEventItem(submission,
                        reposBySubmission.get(submission.getSubmissionId()),
                        submission.getTeamId() != null ? teamsById.get(submission.getTeamId()) : null,
                        roundsById.get(submission.getRoundId())))
                .toList();
    }

    private EventSubmissionRepositoryItemResponse toEventItem(Submissions submission,
                                                              SubmissionRepositoryEntity repoEntity,
                                                              Teams team,
                                                              Round round) {
        LocalDateTime submissionDeadline = round != null ? round.getSubmissionDeadline() : null;
        LocalDateTime lastPushedAt = repoEntity != null ? repoEntity.getLastPushedAt() : null;
        // Chi bao review trung lap, khong phai ket luan: null khi thieu du lieu so sanh.
        Boolean lastPushAfterDeadline = (submissionDeadline != null && lastPushedAt != null)
                ? lastPushedAt.isAfter(submissionDeadline)
                : null;

        String categoryName = null;
        if (round != null && round.getCategory() != null) {
            categoryName = round.getCategory().getCategoryName();
        }

        return EventSubmissionRepositoryItemResponse.builder()
                .submissionId(submission.getSubmissionId())
                .teamId(submission.getTeamId())
                .teamName(team != null ? team.getTeamName() : null)
                .sampleSubmission(Boolean.TRUE.equals(submission.getIsSampleSubmission()))
                .categoryName(categoryName)
                .roundName(round != null ? round.getRoundName() : null)
                .submittedAt(submission.getSubmittedAt())
                .submissionDeadline(submissionDeadline)
                .repositoryUrl(submission.getRepositoryUrl())
                .repository(repoEntity != null ? mapToResponse(repoEntity) : null)
                .lastPushAfterDeadline(lastPushAfterDeadline)
                .build();
    }

    /**
     * Export CSV cho Organizer. Dung lai getEventSubmissionRepositories (da check quyen creator).
     * Moi gia tri deu duoc escape chong CSV injection: gia tri bat dau bang = + - @
     * bi prefix dau nhay don de Excel khong thuc thi nhu formula.
     */
    @Transactional(readOnly = true)
    public String exportEventSubmissionRepositoriesCsv(UUID eventId) {
        List<EventSubmissionRepositoryItemResponse> items = getEventSubmissionRepositories(eventId);
        String eventName = eventRepository.findById(eventId)
                .map(e -> e.getEventName())
                .orElse("");

        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",",
                "Event", "Round", "Category", "Team", "SubmissionID",
                "Provider", "FullName", "RepositoryUrl", "Visibility", "PrimaryLanguage", "DefaultBranch",
                "RepositoryCreatedAt", "RepositoryUpdatedAt", "LastPushedAt", "LastSynchronizedAt",
                "SyncStatus", "ErrorCode", "StarCount", "ForkCount", "OpenIssuesCount",
                "CommitCount", "ContributorCount",
                "SubmittedAt", "LastPushAfterDeadline")).append("\r\n");

        for (EventSubmissionRepositoryItemResponse item : items) {
            SubmissionRepositoryResponse repo = item.getRepository();
            csv.append(String.join(",",
                    csvCell(eventName),
                    csvCell(item.getRoundName()),
                    csvCell(item.getCategoryName()),
                    csvCell(item.getTeamName()),
                    csvCell(item.getSubmissionId()),
                    csvCell(repo != null ? repo.getProvider() : null),
                    csvCell(repo != null ? repo.getFullName() : null),
                    csvCell(repo != null ? repo.getRepositoryUrl() : item.getRepositoryUrl()),
                    csvCell(repo != null ? repo.getVisibility() : null),
                    csvCell(repo != null ? repo.getPrimaryLanguage() : null),
                    csvCell(repo != null ? repo.getDefaultBranch() : null),
                    csvCell(repo != null ? repo.getRepositoryCreatedAt() : null),
                    csvCell(repo != null ? repo.getRepositoryUpdatedAt() : null),
                    csvCell(repo != null ? repo.getLastPushedAt() : null),
                    csvCell(repo != null ? repo.getLastSynchronizedAt() : null),
                    csvCell(repo != null ? repo.getLastSyncStatus() : "MISSING"),
                    csvCell(repo != null ? repo.getErrorCode() : null),
                    csvCell(repo != null ? repo.getStarCount() : null),
                    csvCell(repo != null ? repo.getForkCount() : null),
                    csvCell(repo != null ? repo.getOpenIssuesCount() : null),
                    csvCell(repo != null ? repo.getCommitCount() : null),
                    csvCell(repo != null ? repo.getContributorCount() : null),
                    csvCell(item.getSubmittedAt()),
                    csvCell(item.getLastPushAfterDeadline()))).append("\r\n");
        }
        return csv.toString();
    }

    private String csvCell(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        // Chong CSV injection: khong de Excel/Sheets hieu gia tri la formula.
        if (!text.isEmpty() && (text.charAt(0) == '=' || text.charAt(0) == '+' || text.charAt(0) == '-' || text.charAt(0) == '@')) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public SubmissionRepositoryResponse mapToResponse(SubmissionRepositoryEntity entity) {
        if (entity == null) return null;
        return SubmissionRepositoryResponse.builder()
                .submissionRepositoryId(entity.getSubmissionRepositoryId())
                .submissionId(entity.getSubmission() != null ? entity.getSubmission().getSubmissionId() : null)
                .provider(entity.getProvider() != null ? entity.getProvider().name() : null)
                .externalId(entity.getExternalId())
                .repositoryUrl(entity.getRepositoryUrl())
                .owner(entity.getOwner())
                .repositoryName(entity.getRepositoryName())
                .fullName(entity.getFullName())
                .description(entity.getDescription())
                .visibility(entity.getVisibility())
                .defaultBranch(entity.getDefaultBranch())
                .primaryLanguage(entity.getPrimaryLanguage())
                .repositoryCreatedAt(entity.getRepositoryCreatedAt())
                .repositoryUpdatedAt(entity.getRepositoryUpdatedAt())
                .lastPushedAt(entity.getLastPushedAt())
                .externalUrl(entity.getExternalUrl())
                .lastSyncStatus(entity.getLastSyncStatus() != null ? entity.getLastSyncStatus().name() : null)
                .lastSynchronizedAt(entity.getLastSynchronizedAt())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .starCount(entity.getStarCount())
                .forkCount(entity.getForkCount())
                .openIssuesCount(entity.getOpenIssuesCount())
                .languagesJson(entity.getLanguagesJson())
                .contributorCount(entity.getContributorCount())
                .topContributorsJson(entity.getTopContributorsJson())
                .commitCount(entity.getCommitCount())
                .lastCommitSha(entity.getLastCommitSha())
                .pinnedCommitSha(entity.getPinnedCommitSha())
                .pinnedAt(entity.getPinnedAt())
                .pinnedByUserId(entity.getPinnedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SubmissionRepositoryResponse mapMetadataToResponse(RepositoryMetadata metadata) {
        if (metadata == null) return null;
        return SubmissionRepositoryResponse.builder()
                .provider(metadata.getProvider() != null ? metadata.getProvider().name() : "GITHUB")
                .externalId(metadata.getExternalRepositoryId())
                .repositoryUrl(metadata.getRepositoryUrl())
                .externalUrl(metadata.getExternalUrl())
                .owner(metadata.getOwner())
                .repositoryName(metadata.getRepositoryName())
                .fullName(metadata.getFullName())
                .description(metadata.getDescription())
                .visibility(metadata.getVisibility())
                .defaultBranch(metadata.getDefaultBranch())
                .primaryLanguage(metadata.getPrimaryLanguage())
                .repositoryCreatedAt(metadata.getRepositoryCreatedAt())
                .repositoryUpdatedAt(metadata.getRepositoryUpdatedAt())
                .lastPushedAt(metadata.getLastPushedAt())
                .starCount(metadata.getStarCount())
                .forkCount(metadata.getForkCount())
                .openIssuesCount(metadata.getOpenIssuesCount())
                .lastSyncStatus(RepositorySyncStatus.SUCCESS.name())
                .build();
    }
}
