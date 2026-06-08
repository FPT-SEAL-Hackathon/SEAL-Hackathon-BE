package com.fpt.swp.sealhackathonbe.submission.dto;

import jakarta.validation.constraints.Size;

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
