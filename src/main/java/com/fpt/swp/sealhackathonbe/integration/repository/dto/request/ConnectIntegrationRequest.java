package com.fpt.swp.sealhackathonbe.integration.repository.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectIntegrationRequest {
    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;

    @NotBlank(message = "GitHub Personal Access Token is required")
    private String token;
}
