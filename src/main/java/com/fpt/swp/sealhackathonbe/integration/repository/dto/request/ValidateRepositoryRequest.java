package com.fpt.swp.sealhackathonbe.integration.repository.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateRepositoryRequest {

    @NotBlank(message = "Repository URL is required")
    @Size(max = 500, message = "Repository URL must not exceed 500 characters")
    private String repositoryUrl;
}
