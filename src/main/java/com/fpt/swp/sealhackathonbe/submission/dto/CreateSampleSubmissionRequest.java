package com.fpt.swp.sealhackathonbe.submission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSampleSubmissionRequest {
    // Organizer tao bai mau cho calibration round; khong gan teamId nhung van validate repo nhu bai that.
    @NotNull(message = "Round ID is required")
    private UUID roundId;

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

    private String notes;
}
