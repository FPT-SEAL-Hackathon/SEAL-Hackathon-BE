package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifySubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;

import java.util.UUID;

public interface SubmissionDisqualificationService {
    // Truất quyền một bài nộp cụ thể, không loại cả team.
    SubmissionDisqualificationResponse disqualifySubmission(
            UUID submissionId,
            DisqualifySubmissionRequest request,
            UUID adminUserId
    );
}
