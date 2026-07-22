package com.fpt.swp.sealhackathonbe.integration.repository.client;

import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataErrorCode;
import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GitHubRepositoryUrlParserTest {

    @Test
    @DisplayName("Accepted: Normal HTTPS URL")
    void testNormalHttpsUrl() {
        GitHubRepositoryCoordinates coords = GitHubRepositoryUrlParser.parse("https://github.com/owner/repository");
        assertEquals("owner", coords.getOwner());
        assertEquals("repository", coords.getRepository());
        assertEquals("https://github.com/owner/repository", coords.getNormalizedUrl());
    }

    @Test
    @DisplayName("Accepted: Trailing slash")
    void testTrailingSlash() {
        GitHubRepositoryCoordinates coords = GitHubRepositoryUrlParser.parse("https://github.com/owner/repository/");
        assertEquals("owner", coords.getOwner());
        assertEquals("repository", coords.getRepository());
        assertEquals("https://github.com/owner/repository", coords.getNormalizedUrl());
    }

    @Test
    @DisplayName("Accepted: .git suffix")
    void testGitSuffix() {
        GitHubRepositoryCoordinates coords = GitHubRepositoryUrlParser.parse("https://github.com/owner/repository.git");
        assertEquals("owner", coords.getOwner());
        assertEquals("repository", coords.getRepository());
        assertEquals("https://github.com/owner/repository", coords.getNormalizedUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://github.com/owner/repository",                      // HTTP
            "git@github.com:owner/repository.git",                       // SSH URL
            "github.com/owner/repository",                               // missing scheme
            "https://gitlab.com/owner/repository",                       // wrong host
            "https://evilgithub.com/owner/repository",                   // deceptive host
            "https://github.com.evil.com/owner/repository",              // deceptive subdomain
            "https://www.github.com/owner/repository",                   // www subdomain not accepted
            "https://github.com/owner",                                  // owner only
            "https://github.com/owner/repository/issues",                // extra path segment
            "https://github.com/owner/repository/tree/main",             // extra path segment
            "https://github.com/owner/repository?tab=readme",            // query parameter
            "https://github.com/owner/repository#readme",                // fragment
            "https://github.com:8443/owner/repository",                  // non-standard port
            "https://user:pass@github.com/owner/repository",             // credentials
            "https://github.com/./repository",                           // dot owner
            "https://github.com/owner/..",                               // dot-dot repo
            "https://github.com/owner%2Fsub/repository"                  // path traversal encoded
    })
    @DisplayName("Rejected: Invalid URLs throw RepositoryMetadataException with INVALID_GITHUB_REPOSITORY_URL")
    void testRejectedUrls(String invalidUrl) {
        RepositoryMetadataException ex = assertThrows(
                RepositoryMetadataException.class,
                () -> GitHubRepositoryUrlParser.parse(invalidUrl)
        );
        assertEquals(RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL, ex.getErrorCode());
    }
}
