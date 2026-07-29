package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.CreateSampleSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;

import java.util.UUID;

public interface SubmissionCommandService {
    // Validate request/current user, upsert submission, then return the mapped record.
    SubmissionResponse submitWork(CreateSubmissionRequest request, UUID currentUserId);
    SubmissionResponse submitSampleWork(CreateSampleSubmissionRequest request, UUID currentUserId);
}
