package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubmissionQueryServiceImpl implements SubmissionQueryService {
    // Phan query cua luong submission: repository doc bang Submissions, mapper chuyen entity sang DTO.
    private final SubmissionsRepository submissionsRepository;
    private final TeamMembersRepository teamMembersRepository;

    public SubmissionQueryServiceImpl(
            SubmissionsRepository submissionsRepository,
            TeamMembersRepository teamMembersRepository
    ) {
        this.submissionsRepository = submissionsRepository;
        this.teamMembersRepository = teamMembersRepository;
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
        teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                .orElseThrow(() -> new RuntimeException("User does not belong to this team"));

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
    public List<SubmissionResponse> findByEventId(UUID eventId) {
        // Luong du lieu: EventID -> Team.EventID -> Submissions -> SubmissionResponse.
        return submissionsRepository.findByEventId(eventId)
                .stream()
                .map(SubmissionMapper::toSubmissionResponse)
                .toList();
    }
}
