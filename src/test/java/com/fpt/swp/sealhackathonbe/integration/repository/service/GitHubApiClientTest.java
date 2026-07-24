package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubApiClientTest {

    private final GitHubApiClient apiClient = new GitHubApiClient();

    @Test
    void testParseUrl_Valid() {
        GitHubApiClient.RepoOwnerAndName info = apiClient.parseRepoUrl("https://github.com/facebook/react");
        assertEquals("facebook", info.owner);
        assertEquals("react", info.repo);
    }

    @Test
    void testParseUrl_ValidWithGit() {
        GitHubApiClient.RepoOwnerAndName info = apiClient.parseRepoUrl("https://github.com/facebook/react.git");
        assertEquals("facebook", info.owner);
        assertEquals("react", info.repo);
    }

    @Test
    void testParseUrl_Invalid() {
        assertThrows(RepositoryIntegrationException.class, () -> apiClient.parseRepoUrl("https://gitlab.com/facebook/react"));
    }
}
