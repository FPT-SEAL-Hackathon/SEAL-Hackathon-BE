package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubmissionQueryServiceImpl implements SubmissionQueryService {
    // Phan query cua luong submission: repository doc bang Submissions, mapper chuyen entity sang DTO.
    private final SubmissionsRepository submissionsRepository;

    public SubmissionQueryServiceImpl(SubmissionsRepository submissionsRepository) {
        this.submissionsRepository = submissionsRepository;
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
    public SubmissionResponse getSubmissionByTeamAndRound(UUID teamId, UUID roundId) {
        // Dung unique key o muc bang: moi team chi co mot submission trong mot round.
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
