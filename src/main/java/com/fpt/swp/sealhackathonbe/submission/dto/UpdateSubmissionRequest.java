package com.fpt.swp.sealhackathonbe.submission.dto;

import jakarta.validation.constraints.Size;

// CHUA DUNG TAM THOI:
// Hien tai khong co controller/service nao nhan DTO nay.
// Luong ghi dang thiet ke dung CreateSubmissionRequest voi sp_UpsertSubmission cho ca tao moi va cap nhat.
// Chi nen giu class nay neu sau nay them endpoint PATCH/PUT rieng cho submission.
public class UpdateSubmissionRequest {
    @Size(max = 500, message = "Repository URL must not exceed 500 characters")
    private String repositoryUrl;

    @Size(max = 500, message = "Demo URL must not exceed 500 characters")
    private String demoUrl;

    @Size(max = 500, message = "Report URL must not exceed 500 characters")
    private String reportUrl;

    @Size(max = 500, message = "Slide URL must not exceed 500 characters")
    private String slideUrl;

    private String notes;
}
