package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubmissionQueryServiceImpl implements SubmissionQueryService {
    // Phan query cua luong submission: repository doc bang Submissions, mapper chuyen entity sang DTO.
    private static final UUID SUBMISSION_STATUS_SCORED =
            UUID.fromString("50000000-0000-0000-0000-000000000005");

    private static final UUID SUBMISSION_STATUS_DISQUALIFIED =
            UUID.fromString("50000000-0000-0000-0000-000000000004");

    private final SubmissionsRepository submissionsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final EventParticipantService eventParticipantService;

    public SubmissionQueryServiceImpl(
            SubmissionsRepository submissionsRepository,
            TeamMembersRepository teamMembersRepository,
            EventParticipantService eventParticipantService
    ) {
        this.submissionsRepository = submissionsRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.eventParticipantService = eventParticipantService;
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(UUID submissionId) {
        // Controller -> service -> repository.findById -> mapper -> response.
        return submissionsRepository.findById(submissionId)
                .map(SubmissionMapper::toSubmissionResponse)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionByTeamAndRound(UUID teamId, UUID roundId, UUID currentUserId) {
        // Dung unique key o muc bang: moi team chi co mot submission trong mot round.
        TeamMembers membership = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                .orElseThrow(() -> new RuntimeException("User does not belong to this team"));
        eventParticipantService.assertActiveParticipant(membership.getTeam().getEventId(), currentUserId);

        return submissionsRepository.findByTeamIdAndRoundId(teamId, roundId)
                .map(SubmissionMapper::toSubmissionResponse)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByRound(UUID roundId) {
        // Tra ve tat ca submission trong mot round cho man hinh danh sach/review.
        return submissionsRepository.findByRoundId(roundId)
                .stream()
                .map(SubmissionMapper::toSubmissionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getUnreviewSubmissionByRound(UUID roundId) {
        // Unreview o day la cac submission chua bi cham diem xong va khong bi loai.
        return submissionsRepository
                .findByRoundIdAndSubmissionStatusIdNotIn(
                        roundId,
                        List.of(SUBMISSION_STATUS_SCORED, SUBMISSION_STATUS_DISQUALIFIED)
                )
                .stream()
                .map(SubmissionMapper::toSubmissionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> findByEventId(UUID eventId) {
        // Luong du lieu: EventID -> Team.EventID -> Submissions -> SubmissionResponse.
        return submissionsRepository.findByEventId(eventId)
                .stream()
                .map(SubmissionMapper::toSubmissionResponse)
                .toList();
    }
}
