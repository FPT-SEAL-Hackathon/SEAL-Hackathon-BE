package com.fpt.swp.sealhackathonbe.integration.repository.client;

import lombok.Value;

@Value
public class GitHubRepositoryCoordinates {
    String owner;
    String repository;
    String normalizedUrl;
}
