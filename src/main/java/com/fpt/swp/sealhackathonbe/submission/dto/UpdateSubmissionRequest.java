package com.fpt.swp.sealhackathonbe.submission.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

// CHUA DUNG TAM THOI:
// Hien tai khong co controller/service nao nhan DTO nay.
// Luong ghi dang thiet ke dung CreateSubmissionRequest voi sp_UpsertSubmission cho ca tao moi va cap nhat.
// Chi nen giu class nay neu sau nay them endpoint PATCH/PUT rieng cho submission.
public class UpdateSubmissionRequest {
    @Size(max = 500, message = "Repository URL must not exceed 500 characters")
    @Pattern(
            regexp = SubmissionUrlPatterns.GITHUB_REPOSITORY_URL,
            message = "Repository URL must be a valid GitHub repository URL"
    )
    private String repositoryUrl;

    @Size(max = 500, message = "Demo URL must not exceed 500 characters")
    @Pattern(regexp = "^$|https?://\\S+$", message = "Demo URL must be a valid http(s) URL")
    private String demoUrl;

    @Size(max = 500, message = "Report URL must not exceed 500 characters")
    @Pattern(regexp = "^$|https?://\\S+$", message = "Report URL must be a valid http(s) URL")
    private String reportUrl;

    @Size(max = 500, message = "Slide URL must not exceed 500 characters")
    @Pattern(regexp = "^$|https?://\\S+$", message = "Slide URL must be a valid http(s) URL")
    private String slideUrl;

}
