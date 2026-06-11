package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;

import java.util.UUID;

public interface SubmissionCommandService {
    // Tam thoi chua active khi SubmissionController.submitWork dang bi comment.
    // Luong ghi du kien: validate request/current user, goi sp_UpsertSubmission, tra response da map.
    SubmissionResponse submitWork(CreateSubmissionRequest request, UUID currentUserId);
}
