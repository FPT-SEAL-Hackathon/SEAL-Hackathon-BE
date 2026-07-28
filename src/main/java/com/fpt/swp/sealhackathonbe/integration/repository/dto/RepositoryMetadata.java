package com.fpt.swp.sealhackathonbe.integration.repository.dto;

import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class RepositoryMetadata {
    RepositoryProvider provider;
    String externalRepositoryId;
    String repositoryUrl;
    String externalUrl;
    String owner;
    String repositoryName;
    String fullName;
    String description;
    String visibility;
    String defaultBranch;
    String primaryLanguage;
    LocalDateTime repositoryCreatedAt;
    LocalDateTime repositoryUpdatedAt;
    LocalDateTime lastPushedAt;
    Integer starCount;
    Integer forkCount;
    Integer openIssuesCount;

    // Activity (best-effort, co the null) — gan boi service sau khi goi client.fetchActivity.
    String languagesJson;
    Integer contributorCount;
    String topContributorsJson;
    Integer commitCount;
    String lastCommitSha;
}
