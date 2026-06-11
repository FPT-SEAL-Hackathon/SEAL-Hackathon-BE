package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifySubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;

import java.util.List;
import java.util.UUID;

public interface SubmissionDisqualificationService {
    // Truất quyền một bài nộp cụ thể, không loại cả team.
    SubmissionDisqualificationResponse disqualifySubmission(
            UUID submissionId,
            DisqualifySubmissionRequest request,
            UUID adminUserId
    );

    // Lay cac submission dang co disqualification active, sap xep moi nhat truoc.
    List<DisqualifiedSubmissionResponse> getDisqualifiedSubmissions();
}
