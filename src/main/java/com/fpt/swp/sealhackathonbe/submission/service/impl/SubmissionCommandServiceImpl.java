package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.submission.dto.CreateSampleSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionHistory;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionHistoryRepository;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionCommandService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SubmissionCommandServiceImpl implements SubmissionCommandService {
    // Phan command cua luong submission.
    // currentUserId duoc truyen tu controller sau khi lay user hien tai qua JWT
    // authentication.
    private static final UUID TEAM_STATUS_ACTIVE       = TeamStatusConstants.ACTIVE;
    private static final UUID TEAM_STATUS_DISQUALIFIED = TeamStatusConstants.DISQUALIFIED;
    private static final UUID TEAM_STATUS_REJECTED     = TeamStatusConstants.REJECTED;
    private static final String ROUND_STATUS_SUBMISSION_OPEN = "Submission Open";

    private final SubmissionsRepository submissionsRepository;
    private final SubmissionHistoryRepository submissionHistoryRepository;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final RoundRepository roundRepository;
    private final RoundRankingRepository roundRankingRepository;
    private final EntityManager entityManager;
    private final com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService submissionRepositoryService;
    private final TransactionTemplate transactionTemplate;

    public SubmissionCommandServiceImpl(
            SubmissionsRepository submissionsRepository,
            SubmissionHistoryRepository submissionHistoryRepository,
            TeamsRepository teamsRepository,
            TeamMembersRepository teamMembersRepository,
            RoundRepository roundRepository,
            RoundRankingRepository roundRankingRepository,
            EntityManager entityManager,
            EventParticipantService eventParticipantService,
            com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService submissionRepositoryService,
            PlatformTransactionManager transactionManager) {
        this.submissionsRepository = submissionsRepository;
        this.submissionHistoryRepository = submissionHistoryRepository;
        this.teamsRepository = teamsRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.roundRepository = roundRepository;
        this.roundRankingRepository = roundRankingRepository;
        this.entityManager = entityManager;
        this.submissionRepositoryService = submissionRepositoryService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public SubmissionResponse submitWork(CreateSubmissionRequest request, UUID currentUserId) {
        // Luong ghi:
        // 1. Kiem tra user hien tai la leader active cua team.
        // 2. Kiem tra team da duoc organizer approve va khong bi reject/disqualify.
        // 3. Kiem tra round chua qua deadline nop bai.
        // 4. Fetch metadata GitHub NGOAI transaction (goi HTTP co the mat 15s,
        //    khong duoc giu ket noi/transaction DB trong luc do).
        // 5. Mo MOT transaction ngan: sp_UpsertSubmission + history + metadata repository
        //    duoc commit atomic — khong bao gio commit Submission ma thieu SubmissionRepository.
        Teams team = validateLeaderCanSubmit(request.getTeamId(), currentUserId);
        validateTeamCanSubmit(team);
        Round round = validateTeamCanSubmitToRound(team, request.getRoundId());
        validateRoundAcceptsTeamSubmission(round);

        // Buoc HTTP cham nam o day — sau khi da xac thuc quyen, truoc khi mo transaction.
        final com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadataFetchResult fetchResult =
                (request.getRepositoryUrl() != null && !request.getRepositoryUrl().trim().isEmpty())
                        ? submissionRepositoryService.fetchMetadataOutsideTx(request.getRepositoryUrl())
                        : null;

        return transactionTemplate.execute(status -> {
            // Deadline co the vua troi qua trong luc goi GitHub: kiem tra lai trong transaction.
            validateRoundAcceptsTeamSubmission(round);

            callUpsertSubmissionProcedure(request, currentUserId);

            Submissions submission = submissionsRepository
                    .findByTeamIdAndRoundId(request.getTeamId(), request.getRoundId())
                    .orElseThrow(() -> new RuntimeException("Submission was not created or updated"));

            recordSubmissionHistory(submission);

            SubmissionResponse response = SubmissionMapper.toSubmissionResponse(submission);
            if (fetchResult != null) {
                com.fpt.swp.sealhackathonbe.integration.repository.dto.response.SubmissionRepositoryResponse repoResp =
                        submissionRepositoryService.saveOrUpdateSubmissionRepository(
                                submission.getSubmissionId(), request.getRepositoryUrl(), fetchResult);
                response.setRepository(repoResp);
            }
            return response;
        });
    }

    @Override
    @Transactional
    public SubmissionResponse submitSampleWork(CreateSampleSubmissionRequest request, UUID currentUserId) {
        Round round = findRound(request.getRoundId());
        validateCalibrationRound(round);
        validateRoundAcceptsSampleSubmission(round);

        Submissions sampleSubmission = new Submissions();
        sampleSubmission.setRoundId(request.getRoundId());
        sampleSubmission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        sampleSubmission.setRepositoryUrl(request.getRepositoryUrl());
        sampleSubmission.setDemoUrl(request.getDemoUrl());
        sampleSubmission.setReportUrl(request.getReportUrl());
        sampleSubmission.setSlideUrl(request.getSlideUrl());
        sampleSubmission.setNotes(request.getNotes());
        sampleSubmission.setSubmittedAt(LocalDateTime.now());
        sampleSubmission.setLastUpdatedAt(LocalDateTime.now());
        sampleSubmission.setSubmittedByUserId(currentUserId);
        sampleSubmission.setIsScoreApproved(false);
        sampleSubmission.setIsSampleSubmission(true);

        Submissions saved = submissionsRepository.save(sampleSubmission);
        recordSubmissionHistory(saved);

        return SubmissionMapper.toSubmissionResponse(saved);
    }

    private Teams validateLeaderCanSubmit(UUID teamId, UUID currentUserId) {
        // Chi leader active cua team moi duoc nop hoac cap nhat bai cua team do.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        boolean isMember = teamMembersRepository
                .findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                .isPresent();

        if (!isMember) {
            throw new RuntimeException("User does not belong to this team");
        }

        if (!currentUserId.equals(team.getLeaderUserId())) {
            throw new RuntimeException("Only the team leader can submit work");
        }

        return team;
    }

    private void validateTeamCanSubmit(Teams team) {
        // Only teams approved by organizer for competition can submit or update work.
        if (!TEAM_STATUS_ACTIVE.equals(team.getTeamStatusId())) {
            if (TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())
                    || TEAM_STATUS_REJECTED.equals(team.getTeamStatusId())) {
                throw new RuntimeException("This team cannot submit because it is rejected or disqualified");
            }

            throw new RuntimeException("Only active teams can submit work");
        }
    }

    private Round validateTeamCanSubmitToRound(Teams team, UUID roundId) {
        // A team can only submit to rounds in the same category/event it registered
        // for.
        Round round = findRound(roundId);

        if (round.getCategory() == null || round.getCategory().getCategoryId() == null) {
            throw new RuntimeException("Round category not found");
        }

        if (!team.getCategoryId().equals(round.getCategory().getCategoryId())) {
            throw new RuntimeException("Team cannot submit to a round outside its category");
        }

        if (round.getCategory().getEvent() == null
                || round.getCategory().getEvent().getEventId() == null) {
            throw new RuntimeException("Round event not found");
        }

        if (!team.getEventId().equals(round.getCategory().getEvent().getEventId())) {
            throw new RuntimeException("Team cannot submit to a round outside its event");
        }

        validateTeamAdvancedFromPreviousRound(team, round);

        return round;
    }

    private void validateTeamAdvancedFromPreviousRound(Teams team, Round round) {
        Integer roundOrder = round.getRoundOrder();
        if (roundOrder == null || roundOrder <= 1) {
            return;
        }

        UUID categoryId = round.getCategory().getCategoryId();
        Round previousRound = roundRepository
                .findTopByCategoryCategoryIdAndRoundOrderLessThanOrderByRoundOrderDesc(categoryId, roundOrder)
                .orElseThrow(() -> new RuntimeException("Previous round not found for this round"));

        boolean advanced = roundRankingRepository
                .findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(
                        previousRound.getRoundId(),
                        categoryId,
                        team.getTeamId()
                )
                .map(ranking -> Boolean.TRUE.equals(ranking.getIsAdvanced()))
                .orElse(false);

        if (!advanced) {
            throw new RuntimeException("Team has not advanced from the previous round");
        }
    }

    private Round findRound(UUID roundId) {
        return roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));
    }

    private void validateCalibrationRound(Round round) {
        if (!Boolean.TRUE.equals(round.getIsCalibrationRound())) {
            throw new RuntimeException("Sample submissions are only allowed for calibration rounds");
        }
    }

    private void validateRoundAcceptsSampleSubmission(Round round) {
        String statusName = round.getRoundStatus() != null
                ? round.getRoundStatus().getStatusName()
                : null;

        if ("Judging".equalsIgnoreCase(statusName) || "Completed".equalsIgnoreCase(statusName)) {
            throw new RuntimeException("Cannot create sample submissions after calibration round enters judging or completed status");
        }
    }

    private void validateRoundAcceptsTeamSubmission(Round round) {
        String statusName = round.getRoundStatus() != null
                ? round.getRoundStatus().getStatusName()
                : null;

        if (!ROUND_STATUS_SUBMISSION_OPEN.equalsIgnoreCase(statusName)) {
            throw new RuntimeException("Round is not open for submissions");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = round.getStartDate();
        if (startDate != null && now.isBefore(startDate)) {
            throw new RuntimeException("Round has not started yet");
        }

        LocalDateTime deadline = round.getSubmissionDeadline();
        if (deadline != null && now.isAfter(deadline)) {
            throw new RuntimeException("Submission deadline has passed");
        }
    }

    private void callUpsertSubmissionProcedure(CreateSubmissionRequest request, UUID currentUserId) {
        // Stored procedure quyet dinh insert/update that su.
        // Repository chi duoc dung sau do de lay lai ban ghi da persist.
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("sp_UpsertSubmission");

        query.registerStoredProcedureParameter("TeamID", UUID.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RoundID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepositoryURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("DemoURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("ReportURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("SlideURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("Notes", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoMetadataJSON", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoLastCommitAt", LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoStarCount", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoForkCount", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("SubmittedByUserID", UUID.class, ParameterMode.IN);

        query.setParameter("TeamID", request.getTeamId());
        query.setParameter("RoundID", request.getRoundId().toString());
        query.setParameter("RepositoryURL", request.getRepositoryUrl());
        query.setParameter("DemoURL", request.getDemoUrl());
        query.setParameter("ReportURL", request.getReportUrl());
        query.setParameter("SlideURL", request.getSlideUrl());
        query.setParameter("Notes", request.getNotes());
        query.setParameter("RepoMetadataJSON", null);
        query.setParameter("RepoLastCommitAt", null);
        query.setParameter("RepoStarCount", null);
        query.setParameter("RepoForkCount", null);
        query.setParameter("SubmittedByUserID", currentUserId);

        query.execute();
    }

    private void recordSubmissionHistory(Submissions submission) {
        int nextVersion = submissionHistoryRepository
                .findFirstBySubmissionIdOrderByVersionNumberDesc(submission.getSubmissionId())
                .map(history -> history.getVersionNumber() + 1)
                .orElse(1);

        SubmissionHistory history = new SubmissionHistory();
        history.setSubmissionId(submission.getSubmissionId());
        history.setVersionNumber(nextVersion);
        history.setTeamId(submission.getTeamId());
        history.setRoundId(submission.getRoundId());
        history.setSubmissionStatusId(submission.getSubmissionStatusId());
        history.setRepositoryUrl(submission.getRepositoryUrl());
        history.setDemoUrl(submission.getDemoUrl());
        history.setReportUrl(submission.getReportUrl());
        history.setSlideUrl(submission.getSlideUrl());
        history.setRepoMetadataJson(submission.getRepoMetadataJson());
        history.setRepoLastCommitAt(submission.getRepoLastCommitAt());
        history.setRepoStarCount(submission.getRepoStarCount());
        history.setRepoForkCount(submission.getRepoForkCount());
        history.setSubmittedAt(submission.getSubmittedAt());
        history.setLastUpdatedAt(submission.getLastUpdatedAt());
        history.setSubmittedByUserId(submission.getSubmittedByUserId());
        history.setNotes(submission.getNotes());
        history.setIsScoreApproved(Boolean.TRUE.equals(submission.getIsScoreApproved()));
        history.setIsSampleSubmission(Boolean.TRUE.equals(submission.getIsSampleSubmission()));
        history.setSnapshotCreatedAt(LocalDateTime.now());

        submissionHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public SubmissionResponse approveScore(UUID submissionId, boolean approve) {
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        submission.setIsScoreApproved(approve);
        
        if (approve) {
            submission.setSubmissionStatusId(SubmissionStatusConstants.SCORED);
        } else {
            submission.setSubmissionStatusId(SubmissionStatusConstants.IN_PROGRESS);
        }
        
        submissionsRepository.save(submission);
        return SubmissionMapper.toSubmissionResponse(submission);
    }
}
