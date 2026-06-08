package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;

import java.util.List;
import java.util.UUID;

public interface SubmissionQueryService {
    SubmissionResponse getSubmissionById(UUID submissionId);

    SubmissionResponse getSubmissionByTeamAndRound(UUID teamId, UUID roundId);

    List<SubmissionResponse> getSubmissionsByRound(UUID roundId);
}
