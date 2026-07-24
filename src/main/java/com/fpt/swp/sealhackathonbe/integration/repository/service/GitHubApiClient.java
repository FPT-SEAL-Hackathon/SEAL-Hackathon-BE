package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.GitHubRepoDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GitHubApiClient {

    private final RestClient restClient;
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("^https?://github\\.com/([^/]+)/([^/]+)/?$");

    public GitHubApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl(GITHUB_API_BASE)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .build();
    }

    public static class RepoOwnerAndName {
        public final String owner;
        public final String repo;

        public RepoOwnerAndName(String owner, String repo) {
            this.owner = owner;
            this.repo = repo;
        }
    }

    public static class IssuesPageResult {
        public final List<JsonNode> issues;
        public final boolean hasMore;

        public IssuesPageResult(List<JsonNode> issues, boolean hasMore) {
            this.issues = issues;
            this.hasMore = hasMore;
        }
    }

    public RepoOwnerAndName parseRepoUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Repository URL cannot be empty");
        }
        
        try {
            new URI(url);
        } catch (URISyntaxException e) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Invalid repository URL format");
        }

        Matcher matcher = GITHUB_URL_PATTERN.matcher(url.trim());
        if (!matcher.matches()) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Invalid GitHub repository URL. Expected format: https://github.com/owner/repo");
        }

        String repo = matcher.group(2);
        if (repo.endsWith(".git")) {
            repo = repo.substring(0, repo.length() - 4);
        }
        return new RepoOwnerAndName(matcher.group(1), repo);
    }

    public GitHubRepoDto fetchRepository(String url, String token) {
        RepoOwnerAndName info = parseRepoUrl(url);
        String path = String.format("/repos/%s/%s", info.owner, info.repo);

        try {
            return restClient.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        handleGitHubError(response.getStatusCode(), response.getHeaders());
                    })
                    .body(GitHubRepoDto.class);
        } catch (RestClientResponseException e) {
            handleGitHubError(HttpStatus.valueOf(e.getStatusCode().value()), e.getResponseHeaders());
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR, "Failed to fetch repository");
        } catch (Exception e) {
            if (e instanceof RepositoryIntegrationException) throw e;
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR, "Error communicating with GitHub: " + e.getMessage());
        }
    }

    public IssuesPageResult fetchIssuesPage(String owner, String repo, String token, int page, int perPage) {
        String path = String.format("/repos/%s/%s/issues?state=all&sort=updated&direction=desc&page=%d&per_page=%d",
                owner, repo, page, perPage);

        try {
            ResponseEntity<String> responseEntity = restClient.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        handleGitHubError(response.getStatusCode(), response.getHeaders());
                    })
                    .toEntity(String.class);

            List<JsonNode> issues = new ArrayList<>();
            if (responseEntity.getBody() != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                try {
                    JsonNode rootNode = mapper.readTree(responseEntity.getBody());
                    if (rootNode != null && rootNode.isArray()) {
                        for (JsonNode node : rootNode) {
                            // Exclude pull requests (GitHub API returns PRs in the issues endpoint)
                            if (!node.has("pull_request")) {
                                issues.add(node);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR, "Invalid JSON response from GitHub API", e);
                }
            }

            // Check Link header for next page
            boolean hasMore = false;
            List<String> linkHeaders = responseEntity.getHeaders().get(HttpHeaders.LINK);
            if (linkHeaders != null && !linkHeaders.isEmpty()) {
                String linkHeader = linkHeaders.get(0);
                if (linkHeader != null && linkHeader.contains("rel=\"next\"")) {
                    hasMore = true;
                }
            }

            return new IssuesPageResult(issues, hasMore);
        } catch (RestClientResponseException e) {
            handleGitHubError(HttpStatus.valueOf(e.getStatusCode().value()), e.getResponseHeaders());
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR, "Failed to fetch issues");
        } catch (Exception e) {
            if (e instanceof RepositoryIntegrationException) throw e;
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR, "Error communicating with GitHub: " + e.getMessage());
        }
    }

    private void handleGitHubError(HttpStatusCode statusCode, HttpHeaders headers) {
        String retryAfter = null;
        if (headers != null) {
            String retryAfterHeader = headers.getFirst("Retry-After");
            if (retryAfterHeader != null) {
                retryAfter = retryAfterHeader;
            } else {
                String rateLimitReset = headers.getFirst("x-ratelimit-reset");
                if (rateLimitReset != null) {
                    retryAfter = rateLimitReset;
                }
            }
        }

        if (statusCode == HttpStatus.UNAUTHORIZED) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_TOKEN, "Invalid or revoked GitHub token");
        } else if (statusCode == HttpStatus.FORBIDDEN) {
            if (retryAfter != null || (headers != null && "0".equals(headers.getFirst("x-ratelimit-remaining")))) {
                throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_RATE_LIMITED, "GitHub API rate limit exceeded", retryAfter);
            }
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_REPOSITORY_FORBIDDEN, "Insufficient repository permission");
        } else if (statusCode == HttpStatus.NOT_FOUND) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_REPOSITORY_NOT_FOUND, "Repository not found or inaccessible");
        } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_RATE_LIMITED, "GitHub API rate limit exceeded", retryAfter);
        } else if (statusCode == HttpStatus.BAD_GATEWAY || statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR, "GitHub API is currently unavailable");
        } else if (statusCode == HttpStatus.GATEWAY_TIMEOUT) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_TIMEOUT, "GitHub API request timed out");
        } else if (statusCode.is4xxClientError()) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Invalid request to GitHub API");
        } else {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR, "Unexpected error from GitHub API: " + statusCode.value());
        }
    }
}
