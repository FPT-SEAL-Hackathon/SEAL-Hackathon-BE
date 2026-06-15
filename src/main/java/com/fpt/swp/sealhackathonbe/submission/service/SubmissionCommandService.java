package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;

import java.util.UUID;

public interface SubmissionCommandService {
    // Validate request/current user, goi sp_UpsertSubmission va tra ban ghi da duoc map.
    SubmissionResponse submitWork(CreateSubmissionRequest request, UUID currentUserId);
}
