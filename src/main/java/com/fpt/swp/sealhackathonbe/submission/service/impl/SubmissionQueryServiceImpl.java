package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionHistoryResponse;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;

import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionHistoryRepository;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SubmissionQueryServiceImpl implements SubmissionQueryService {
    // Phan query cua luong submission: repository doc bang Submissions, mapper chuyen entity sang DTO.
    private static final UUID SUBMISSION_STATUS_SCORED =
            SubmissionStatusConstants.SCORED;

    private static final UUID SUBMISSION_STATUS_DISQUALIFIED =
            SubmissionStatusConstants.DISQUALIFIED;
    private static final UUID TEAM_STATUS_DISQUALIFIED =
            TeamStatusConstants.DISQUALIFIED;
    private static final UUID TEAM_STATUS_WITHDRAWN =
            TeamStatusConstants.WITHDRAWN;

    private final SubmissionsRepository submissionsRepository;
    private final SubmissionHistoryRepository submissionHistoryRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final DisqualificationsRepository disqualificationsRepository;
    private final EventParticipantService eventParticipantService;
    private final com.fpt.swp.sealhackathonbe.integration.repository.repository.SubmissionRepositoryEntityRepository submissionRepositoryEntityRepository;
    private final com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService submissionRepositoryService;

    public SubmissionQueryServiceImpl(
            SubmissionsRepository submissionsRepository,
            SubmissionHistoryRepository submissionHistoryRepository,
            TeamMembersRepository teamMembersRepository,
            DisqualificationsRepository disqualificationsRepository,
            EventParticipantService eventParticipantService,
            com.fpt.swp.sealhackathonbe.integration.repository.repository.SubmissionRepositoryEntityRepository submissionRepositoryEntityRepository,
            com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService submissionRepositoryService
    ) {
        this.submissionsRepository = submissionsRepository;
        this.submissionHistoryRepository = submissionHistoryRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.disqualificationsRepository = disqualificationsRepository;
        this.eventParticipantService = eventParticipantService;
        this.submissionRepositoryEntityRepository = submissionRepositoryEntityRepository;
        this.submissionRepositoryService = submissionRepositoryService;
    }

    private SubmissionResponse enrichResponse(Submissions submission) {
        SubmissionResponse response = SubmissionMapper.toSubmissionResponse(submission);
        if (response != null && response.getSubmissionId() != null) {
            submissionRepositoryEntityRepository.findBySubmission_SubmissionId(response.getSubmissionId())
                    .ifPresent(repoEntity -> response.setRepository(submissionRepositoryService.mapToResponse(repoEntity)));
            disqualificationsRepository.findActiveBySubmissionIdOrderByDisqualifiedAtDesc(response.getSubmissionId())
                    .stream()
                    .findFirst()
                    .ifPresent(disqualification -> applyActiveDisqualification(response, disqualification));
        }
        return response;
    }

    // Enrich cho danh sach: MOT query batch lay het metadata theo submissionIds
    // thay vi N query rieng le (N+1) nhu enrichResponse tung item.
    private List<SubmissionResponse> enrichResponses(List<Submissions> submissions) {
        List<SubmissionResponse> responses = submissions.stream()
                .map(SubmissionMapper::toSubmissionResponse)
                .toList();

        List<UUID> ids = responses.stream()
                .map(SubmissionResponse::getSubmissionId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return responses;
        }

        var reposBySubmissionId = submissionRepositoryEntityRepository.findBySubmission_SubmissionIdIn(ids).stream()
                .collect(Collectors.toMap(
                        repo -> repo.getSubmission().getSubmissionId(),
                        repo -> repo));
        Map<UUID, Disqualifications> disqualificationsBySubmissionId =
                disqualificationsRepository.findActiveBySubmissionIdInOrderByDisqualifiedAtDesc(ids).stream()
                        .collect(Collectors.toMap(
                                Disqualifications::getSubmissionId,
                                Function.identity(),
                                (first, ignored) -> first
                        ));
        responses.forEach(response -> {
            var repoEntity = reposBySubmissionId.get(response.getSubmissionId());
            if (repoEntity != null) {
                response.setRepository(submissionRepositoryService.mapToResponse(repoEntity));
            }
            var disqualification = disqualificationsBySubmissionId.get(response.getSubmissionId());
            if (disqualification != null) {
                applyActiveDisqualification(response, disqualification);
            }
        });
        return responses;
    }

    private void applyActiveDisqualification(
            SubmissionResponse response,
            Disqualifications disqualification
    ) {
        response.setActiveDisqualificationId(disqualification.getDisqualificationId());
        response.setActiveDisqualificationReason(disqualification.getReason());
        response.setActiveDisqualifiedById(disqualification.getDisqualifiedById());
        response.setActiveDisqualifiedAt(disqualification.getDisqualifiedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(UUID submissionId) {
        // Controller -> service -> repository.findById -> mapper -> response.
        return submissionsRepository.findById(submissionId)
                .map(this::enrichResponse)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionByTeamAndRound(UUID teamId, UUID roundId, UUID currentUserId) {
        // Dung unique key o muc bang: moi team chi co mot submission trong mot round.
        TeamMembers membership = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                .orElseThrow(() -> new RuntimeException("User does not belong to this team"));
        if (!canViewReadOnlyAfterTeamExit(membership)) {
            eventParticipantService.assertActiveParticipant(membership.getTeam().getEventId(), currentUserId);
        }

        return submissionsRepository.findByTeamIdAndRoundId(teamId, roundId)
                .map(this::enrichResponse)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    private boolean canViewReadOnlyAfterTeamExit(TeamMembers membership) {
        UUID teamStatusId = membership.getTeam() != null
                ? membership.getTeam().getTeamStatusId()
                : null;
        return TEAM_STATUS_WITHDRAWN.equals(teamStatusId)
                || TEAM_STATUS_DISQUALIFIED.equals(teamStatusId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByRound(UUID roundId) {
        // Tra ve tat ca submission trong mot round cho man hinh danh sach/review.
        return enrichResponses(submissionsRepository.findByRoundId(roundId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getUnreviewSubmissionByRound(UUID roundId) {
        // Unreview o day la cac submission chua bi cham diem xong va khong bi loai.
        return enrichResponses(submissionsRepository
                .findByRoundIdAndSubmissionStatusIdNotIn(
                        roundId,
                        List.of(SUBMISSION_STATUS_SCORED, SUBMISSION_STATUS_DISQUALIFIED)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> findByEventId(UUID eventId) {
        // Luong du lieu: EventID -> Team.EventID -> Submissions -> SubmissionResponse.
        return enrichResponses(submissionsRepository.findByEventId(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionHistoryResponse> getSubmissionHistoryByTeamAndRound(UUID teamId, UUID roundId, UUID currentUserId) {
        teamMembersRepository.findByTeamIdAndUserId(teamId, currentUserId)
                .orElseThrow(() -> new RuntimeException("User does not belong to this team"));

        return submissionHistoryRepository.findByTeamIdAndRoundIdOrderByVersionNumberDesc(teamId, roundId)
                .stream()
                .map(SubmissionMapper::toSubmissionHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionHistoryResponse> getSubmissionHistoryBySubmissionId(UUID submissionId) {
        return submissionHistoryRepository.findBySubmissionIdOrderByVersionNumberDesc(submissionId)
                .stream()
                .map(SubmissionMapper::toSubmissionHistoryResponse)
                .toList();
    }
}
