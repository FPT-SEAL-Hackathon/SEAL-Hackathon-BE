package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;

import java.util.UUID;

public interface SubmissionCommandService {
    SubmissionResponse submitWork(CreateSubmissionRequest request, UUID currentUserId);
}
