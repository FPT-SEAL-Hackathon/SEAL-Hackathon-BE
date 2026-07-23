package com.fpt.swp.sealhackathonbe.integration.repository.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubIssueDto {
    private Long id;
    private Integer number;
    private String title;
    private String state;
    @JsonProperty("html_url")
    private String htmlUrl;
    
    // If pull_request is present, it's a PR, not a pure issue.
    @JsonProperty("pull_request")
    private Object pullRequest;
}
