package com.fpt.swp.sealhackathonbe.integration.repository.client;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;
import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataErrorCode;
import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GitHubRepositoryMetadataClientTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private GitHubRepositoryMetadataClient metadataClient;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "SEAL-Hackathon")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token-123");
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        metadataClient = new GitHubRepositoryMetadataClient(restClientBuilder.build());
    }

    @Test
    @DisplayName("Valid public repository response mapped correctly")
    void testValidPublicRepository() {
        String jsonResponse = """
                {
                  "id": 123456,
                  "name": "seal-hackathon-be",
                  "full_name": "fpt-seal/seal-hackathon-be",
                  "private": false,
                  "owner": {
                    "login": "fpt-seal"
                  },
                  "html_url": "https://github.com/fpt-seal/seal-hackathon-be",
                  "description": "Backend service for SEAL Hackathon Manager",
                  "visibility": "public",
                  "default_branch": "prod",
                  "language": "Java",
                  "created_at": "2026-01-15T10:00:00Z",
                  "updated_at": "2026-07-22T11:42:00Z",
                  "pushed_at": "2026-07-22T11:45:00Z",
                  "stargazers_count": 42,
                  "forks_count": 5,
                  "open_issues_count": 3
                }
                """;

        mockServer.expect(requestTo("https://api.github.com/repos/fpt-seal/seal-hackathon-be"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, "application/vnd.github+json"))
                .andExpect(header("X-GitHub-Api-Version", "2022-11-28"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token-123"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        RepositoryMetadata metadata = metadataClient.fetchPublicMetadata("https://github.com/fpt-seal/seal-hackathon-be");

        assertNotNull(metadata);
        assertEquals(RepositoryProvider.GITHUB, metadata.getProvider());
        assertEquals("123456", metadata.getExternalRepositoryId());
        assertEquals("https://github.com/fpt-seal/seal-hackathon-be", metadata.getRepositoryUrl());
        assertEquals("fpt-seal", metadata.getOwner());
        assertEquals("seal-hackathon-be", metadata.getRepositoryName());
        assertEquals("fpt-seal/seal-hackathon-be", metadata.getFullName());
        assertEquals("PUBLIC", metadata.getVisibility());
        assertEquals("prod", metadata.getDefaultBranch());
        assertEquals("Java", metadata.getPrimaryLanguage());
        assertEquals(42, metadata.getStarCount());
        assertEquals(5, metadata.getForkCount());
        assertEquals(3, metadata.getOpenIssuesCount());
        assertEquals(LocalDateTime.of(2026, 1, 15, 10, 0, 0), metadata.getRepositoryCreatedAt());
        assertEquals(LocalDateTime.of(2026, 7, 22, 11, 42, 0), metadata.getRepositoryUpdatedAt());
        assertEquals(LocalDateTime.of(2026, 7, 22, 11, 45, 0), metadata.getLastPushedAt());

        mockServer.verify();
    }

    @Test
    @DisplayName("Private repository is rejected with PRIVATE_REPOSITORY_NOT_SUPPORTED")
    void testPrivateRepositoryRejected() {
        String jsonResponse = """
                {
                  "id": 123456,
                  "name": "private-repo",
                  "full_name": "fpt-seal/private-repo",
                  "private": true,
                  "owner": { "login": "fpt-seal" },
                  "html_url": "https://github.com/fpt-seal/private-repo",
                  "default_branch": "main",
                  "created_at": "2026-01-15T10:00:00Z",
                  "updated_at": "2026-07-22T11:42:00Z"
                }
                """;

        mockServer.expect(requestTo("https://api.github.com/repos/fpt-seal/private-repo"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        RepositoryMetadataException ex = assertThrows(
                RepositoryMetadataException.class,
                () -> metadataClient.fetchPublicMetadata("https://github.com/fpt-seal/private-repo")
        );

        assertEquals(RepositoryMetadataErrorCode.PRIVATE_REPOSITORY_NOT_SUPPORTED, ex.getErrorCode());
    }

    @Test
    @DisplayName("GitHub 404 mapped to GITHUB_REPOSITORY_NOT_FOUND")
    void testNotFoundMapping() {
        mockServer.expect(requestTo("https://api.github.com/repos/fpt-seal/nonexistent"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        RepositoryMetadataException ex = assertThrows(
                RepositoryMetadataException.class,
                () -> metadataClient.fetchPublicMetadata("https://github.com/fpt-seal/nonexistent")
        );

        assertEquals(RepositoryMetadataErrorCode.GITHUB_REPOSITORY_NOT_FOUND, ex.getErrorCode());
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    @DisplayName("GitHub 403 with rate limit header mapped to GITHUB_RATE_LIMITED")
    void testRateLimitMapping() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Remaining", "0");

        mockServer.expect(requestTo("https://api.github.com/repos/fpt-seal/popular-repo"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).headers(headers));

        RepositoryMetadataException ex = assertThrows(
                RepositoryMetadataException.class,
                () -> metadataClient.fetchPublicMetadata("https://github.com/fpt-seal/popular-repo")
        );

        assertEquals(RepositoryMetadataErrorCode.GITHUB_RATE_LIMITED, ex.getErrorCode());
        assertEquals(403, ex.getHttpStatus());
        assertEquals("0", ex.getRateLimitRemaining());
    }

    @Test
    @DisplayName("GitHub 429 mapped to GITHUB_RATE_LIMITED")
    void testStatus429Mapping() {
        mockServer.expect(requestTo("https://api.github.com/repos/fpt-seal/popular-repo"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        RepositoryMetadataException ex = assertThrows(
                RepositoryMetadataException.class,
                () -> metadataClient.fetchPublicMetadata("https://github.com/fpt-seal/popular-repo")
        );

        assertEquals(RepositoryMetadataErrorCode.GITHUB_RATE_LIMITED, ex.getErrorCode());
        assertEquals(429, ex.getHttpStatus());
    }

    @Test
    @DisplayName("GitHub 500 mapped to GITHUB_UPSTREAM_ERROR")
    void testUpstreamErrorMapping() {
        mockServer.expect(requestTo("https://api.github.com/repos/fpt-seal/repo"))
                .andRespond(withServerError());

        RepositoryMetadataException ex = assertThrows(
                RepositoryMetadataException.class,
                () -> metadataClient.fetchPublicMetadata("https://github.com/fpt-seal/repo")
        );

        assertEquals(RepositoryMetadataErrorCode.GITHUB_UPSTREAM_ERROR, ex.getErrorCode());
        assertEquals(500, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Repository identity mismatch throws GITHUB_INVALID_RESPONSE")
    void testIdentityMismatch() {
        String jsonResponse = """
                {
                  "id": 123456,
                  "name": "different-repo",
                  "full_name": "fpt-seal/different-repo",
                  "private": false,
                  "owner": { "login": "fpt-seal" },
                  "html_url": "https://github.com/fpt-seal/different-repo",
                  "default_branch": "main",
                  "created_at": "2026-01-15T10:00:00Z",
                  "updated_at": "2026-07-22T11:42:00Z"
                }
                """;

        mockServer.expect(requestTo("https://api.github.com/repos/fpt-seal/requested-repo"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        RepositoryMetadataException ex = assertThrows(
                RepositoryMetadataException.class,
                () -> metadataClient.fetchPublicMetadata("https://github.com/fpt-seal/requested-repo")
        );

        assertEquals(RepositoryMetadataErrorCode.GITHUB_INVALID_RESPONSE, ex.getErrorCode());
    }
}
