package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.submission.dto.CreateSampleSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionCommandService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionHistoryService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
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
    private static final UUID TEAM_STATUS_WITHDRAWN    = TeamStatusConstants.WITHDRAWN;
    private static final String ROUND_STATUS_SUBMISSION_OPEN = "Submission Open";

    private final SubmissionsRepository submissionsRepository;
    private final SubmissionHistoryService submissionHistoryService;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final RoundRepository roundRepository;
    private final RoundRankingRepository roundRankingRepository;
    private final DisqualificationsRepository disqualificationsRepository;
    private final com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService submissionRepositoryService;
    private final TransactionTemplate transactionTemplate;

    public SubmissionCommandServiceImpl(
            SubmissionsRepository submissionsRepository,
            SubmissionHistoryService submissionHistoryService,
            TeamsRepository teamsRepository,
            TeamMembersRepository teamMembersRepository,
            RoundRepository roundRepository,
            RoundRankingRepository roundRankingRepository,
            DisqualificationsRepository disqualificationsRepository,
            EventParticipantService eventParticipantService,
            com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService submissionRepositoryService,
            PlatformTransactionManager transactionManager) {
        this.submissionsRepository = submissionsRepository;
        this.submissionHistoryService = submissionHistoryService;
        this.teamsRepository = teamsRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.roundRepository = roundRepository;
        this.roundRankingRepository = roundRankingRepository;
        this.disqualificationsRepository = disqualificationsRepository;
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
        // 5. Mo MOT transaction ngan: upsert submission + history + metadata repository
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

            Submissions submission = upsertSubmission(request, currentUserId);

            submissionHistoryService.recordSnapshot(submission);

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
    public SubmissionResponse submitSampleWork(CreateSampleSubmissionRequest request, UUID currentUserId) {
        // Sample submission (bài mẫu của calibration round) cần metadata repository GIỐNG bài
        // thật để judge chấm calibration thấy được RepositoryMetadataCard. Fetch GitHub NGOÀI
        // transaction rồi persist atomic bằng transactionTemplate — cùng pattern với submitWork.
        Round round = findRound(request.getRoundId());
        validateCalibrationRound(round);
        validateRoundAcceptsSampleSubmission(round);

        // Buoc HTTP cham nam ngoai transaction.
        final com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadataFetchResult fetchResult =
                (request.getRepositoryUrl() != null && !request.getRepositoryUrl().trim().isEmpty())
                        ? submissionRepositoryService.fetchMetadataOutsideTx(request.getRepositoryUrl())
                        : null;

        return transactionTemplate.execute(status -> {
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
            submissionHistoryService.recordSnapshot(saved);

            SubmissionResponse response = SubmissionMapper.toSubmissionResponse(saved);
            if (fetchResult != null) {
                com.fpt.swp.sealhackathonbe.integration.repository.dto.response.SubmissionRepositoryResponse repoResp =
                        submissionRepositoryService.saveOrUpdateSubmissionRepository(
                                saved.getSubmissionId(), request.getRepositoryUrl(), fetchResult);
                response.setRepository(repoResp);
            }
            return response;
        });
    }

    private Teams validateLeaderCanSubmit(UUID teamId, UUID currentUserId) {
        // Chi leader active cua team moi duoc nop hoac cap nhat bai cua team do.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        boolean isMember = teamMembersRepository
                .findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                .isPresent();

        if (!isMember) {
            throw new AccessDeniedException("User does not belong to this team");
        }

        if (!currentUserId.equals(team.getLeaderUserId())) {
            throw new AccessDeniedException("Only the team leader can submit work");
        }

        return team;
    }

    private void validateTeamCanSubmit(Teams team) {
        // Only teams approved by organizer for competition can submit or update work.
        if (!TEAM_STATUS_ACTIVE.equals(team.getTeamStatusId())) {
            if (TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())
                    || TEAM_STATUS_REJECTED.equals(team.getTeamStatusId())) {
                throw new BusinessConflictException("This team cannot submit because it is rejected or disqualified");
            }

            throw new BusinessConflictException("Only active teams can submit work");
        }
    }

    private Round validateTeamCanSubmitToRound(Teams team, UUID roundId) {
        // A team can only submit to rounds in the same category/event it registered
        // for.
        Round round = findRound(roundId);

        if (round.getCategory() == null || round.getCategory().getCategoryId() == null) {
            throw new EntityNotFoundException("Round category not found");
        }

        if (!team.getCategoryId().equals(round.getCategory().getCategoryId())) {
            throw new BusinessConflictException("Team cannot submit to a round outside its category");
        }

        if (round.getCategory().getEvent() == null
                || round.getCategory().getEvent().getEventId() == null) {
            throw new EntityNotFoundException("Round event not found");
        }

        if (!team.getEventId().equals(round.getCategory().getEvent().getEventId())) {
            throw new BusinessConflictException("Team cannot submit to a round outside its event");
        }

        validateTeamAdvancedFromPreviousRound(team, round);

        return round;
    }

    private void validateTeamAdvancedFromPreviousRound(Teams team, Round round) {
        if (Boolean.TRUE.equals(round.getIsCalibrationRound())) {
            throw new BusinessConflictException("Teams cannot submit work to calibration rounds");
        }

        UUID categoryId = round.getCategory().getCategoryId();
        Integer roundOrder = round.getRoundOrder();
        if (roundOrder == null) {
            throw new BadRequestException("Round order is required for submissions");
        }

        Round previousCompetitionRound = roundRepository
                .findTopByCategoryCategoryIdAndRoundOrderLessThanAndIsCalibrationRoundFalseOrderByRoundOrderDesc(
                        categoryId,
                        roundOrder
                )
                .orElse(null);

        if (previousCompetitionRound == null) {
            return;
        }

        boolean advanced = roundRankingRepository
                .findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(
                        previousCompetitionRound.getRoundId(),
                        categoryId,
                        team.getTeamId()
                )
                .map(ranking -> Boolean.TRUE.equals(ranking.getIsApproved())
                        && Boolean.TRUE.equals(ranking.getIsAdvanced()))
                .orElse(false);

        if (!advanced) {
            throw new BusinessConflictException("Team has not advanced from the previous competition round");
        }
    }

    private Round findRound(UUID roundId) {
        return roundRepository.findByIdWithCategoryEventAndStatus(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
    }

    private void validateCalibrationRound(Round round) {
        if (!Boolean.TRUE.equals(round.getIsCalibrationRound())) {
            throw new BusinessConflictException("Sample submissions are only allowed for calibration rounds");
        }
    }

    private void validateRoundAcceptsSampleSubmission(Round round) {
        String statusName = round.getRoundStatus() != null
                ? round.getRoundStatus().getStatusName()
                : null;

        if ("Judging".equalsIgnoreCase(statusName) || "Completed".equalsIgnoreCase(statusName)) {
            throw new BusinessConflictException("Cannot create sample submissions after calibration round enters judging or completed status");
        }
    }

    private void validateRoundAcceptsTeamSubmission(Round round) {
        String statusName = round.getRoundStatus() != null
                ? round.getRoundStatus().getStatusName()
                : null;

        if (!ROUND_STATUS_SUBMISSION_OPEN.equalsIgnoreCase(statusName)) {
            throw new BusinessConflictException("Round is not open for submissions");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = round.getStartDate();
        if (startDate != null && now.isBefore(startDate)) {
            throw new BusinessConflictException("Round has not started yet");
        }

        LocalDateTime deadline = round.getSubmissionDeadline();
        if (deadline != null && now.isAfter(deadline)) {
            throw new BusinessConflictException("Submission deadline has passed");
        }
    }

    private Submissions upsertSubmission(CreateSubmissionRequest request, UUID currentUserId) {
        Submissions submission = submissionsRepository
                .findByTeamIdAndRoundId(request.getTeamId(), request.getRoundId())
                .orElseGet(Submissions::new);
        validateSubmissionCanBeUpdated(submission);
        LocalDateTime now = LocalDateTime.now();

        submission.setTeamId(request.getTeamId());
        submission.setRoundId(request.getRoundId());
        submission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        submission.setRepositoryUrl(request.getRepositoryUrl());
        submission.setDemoUrl(request.getDemoUrl());
        submission.setReportUrl(request.getReportUrl());
        submission.setSlideUrl(request.getSlideUrl());
        submission.setNotes(request.getNotes());
        submission.setRepoMetadataJson(null);
        submission.setRepoLastCommitAt(null);
        submission.setRepoStarCount(null);
        submission.setRepoForkCount(null);
        submission.setSubmittedAt(now);
        submission.setLastUpdatedAt(now);
        submission.setSubmittedByUserId(currentUserId);
        submission.setIsScoreApproved(false);
        submission.setIsSampleSubmission(false);

        return submissionsRepository.save(submission);
    }

    private void validateSubmissionCanBeUpdated(Submissions submission) {
        if (submission.getSubmissionId() == null) {
            return;
        }

        if (SubmissionStatusConstants.DISQUALIFIED.equals(submission.getSubmissionStatusId())
                || !disqualificationsRepository
                        .findActiveBySubmissionIdOrderByDisqualifiedAtDesc(submission.getSubmissionId())
                        .isEmpty()) {
            throw new BusinessConflictException("This submission has been disqualified and cannot be updated");
        }
    }

    @Override
    @Transactional
    public SubmissionResponse approveScore(UUID submissionId, boolean approve) {
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
        validateSubmissionScoreCanBeChanged(submission);

        submission.setIsScoreApproved(approve);
        
        if (approve) {
            submission.setSubmissionStatusId(SubmissionStatusConstants.SCORED);
        } else {
            submission.setSubmissionStatusId(SubmissionStatusConstants.IN_PROGRESS);
        }
        
        submissionsRepository.save(submission);
        return SubmissionMapper.toSubmissionResponse(submission);
    }

    private void validateSubmissionScoreCanBeChanged(Submissions submission) {
        if (SubmissionStatusConstants.DISQUALIFIED.equals(submission.getSubmissionStatusId())) {
            throw new BusinessConflictException("Disqualified submissions cannot have scores approved or rejected");
        }

        UUID teamStatusId = submission.getTeam() != null
                ? submission.getTeam().getTeamStatusId()
                : null;
        if (TEAM_STATUS_DISQUALIFIED.equals(teamStatusId) || TEAM_STATUS_WITHDRAWN.equals(teamStatusId)) {
            throw new BusinessConflictException("Submissions from disqualified or withdrawn teams cannot have scores approved or rejected");
        }
    }
}
