package com.fpt.swp.sealhackathonbe.submission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSubmissionRequest {
    // Du lieu dau vao cho luong submitWork.
    // Controller nhan DTO nay, command service validate, sau do stored procedure tao moi/cap nhat ban ghi.
    @NotNull(message = "Team ID is required")
    private UUID teamId;

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
