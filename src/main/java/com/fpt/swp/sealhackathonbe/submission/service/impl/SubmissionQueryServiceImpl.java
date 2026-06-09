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
    private final SubmissionsRepository submissionsRepository;

    public SubmissionQueryServiceImpl(SubmissionsRepository submissionsRepository) {
        this.submissionsRepository = submissionsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(UUID submissionId) {
        return submissionsRepository.findById(submissionId)
                .map(SubmissionMapper::toSubmissionResponse)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionByTeamAndRound(UUID teamId, UUID roundId) {
        return submissionsRepository.findByTeamIdAndRoundId(teamId, roundId)
                .map(SubmissionMapper::toSubmissionResponse)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByRound(UUID roundId) {
        return submissionsRepository.findByRoundId(roundId)
                .stream()
                .map(SubmissionMapper::toSubmissionResponse)
                .toList();
    }
}
