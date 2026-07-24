package com.fpt.swp.sealhackathonbe.integration.repository.client;

import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataErrorCode;
import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataException;

import java.net.URI;
import java.util.regex.Pattern;

public class GitHubRepositoryUrlParser {

    private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");

    private GitHubRepositoryUrlParser() {
    }

    public static GitHubRepositoryCoordinates parse(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Repository URL must not be empty"
            );
        }

        String trimmed = rawUrl.trim();

        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (Exception e) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Invalid URI syntax: " + trimmed,
                    e
            );
        }

        // Scheme MUST be https
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Only HTTPS scheme is accepted"
            );
        }

        // Host MUST be github.com exactly
        if (uri.getHost() == null || !"github.com".equalsIgnoreCase(uri.getHost())) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Only github.com domain is supported"
            );
        }

        // No user info (credentials)
        if (uri.getUserInfo() != null) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "URL credentials are not allowed"
            );
        }

        // Default port only (-1)
        if (uri.getPort() != -1) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Non-standard ports are not allowed"
            );
        }

        // No query parameters
        if (uri.getRawQuery() != null && !uri.getRawQuery().isEmpty()) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Query parameters are not allowed"
            );
        }

        // No fragment
        if (uri.getRawFragment() != null && !uri.getRawFragment().isEmpty()) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "URL fragments are not allowed"
            );
        }

        String path = uri.getPath();
        if (path == null) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Path is missing"
            );
        }

        // Remove leading and trailing slashes
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.isEmpty()) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "Repository path cannot be empty"
            );
        }

        String[] segments = path.split("/");
        if (segments.length != 2) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    "URL must contain exactly owner and repository path segments"
            );
        }

        String owner = segments[0];
        String repository = segments[1];

        // Strip trailing .git suffix
        if (repository.toLowerCase().endsWith(".git")) {
            repository = repository.substring(0, repository.length() - 4);
        }

        validateCoordinate(owner, "owner");
        validateCoordinate(repository, "repository");

        String normalizedUrl = "https://github.com/" + owner + "/" + repository;
        return new GitHubRepositoryCoordinates(owner, repository, normalizedUrl);
    }

    private static void validateCoordinate(String val, String fieldName) {
        if (val == null || val.trim().isEmpty()) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    fieldName + " cannot be empty"
            );
        }
        if (".".equals(val) || "..".equals(val)) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    fieldName + " cannot be dot or dot-dot"
            );
        }
        if (!SAFE_NAME_PATTERN.matcher(val).matches()) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.INVALID_GITHUB_REPOSITORY_URL,
                    fieldName + " contains invalid characters: " + val
            );
        }
    }
}
