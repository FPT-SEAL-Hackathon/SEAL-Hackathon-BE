package com.fpt.swp.sealhackathonbe.core.exception;

import lombok.Getter;

@Getter
public class RepositoryIntegrationException extends RuntimeException {

    public enum ErrorCode {
        INVALID_GITHUB_REPOSITORY_URL,
        INVALID_GITHUB_TOKEN,
        GITHUB_REPOSITORY_FORBIDDEN,
        GITHUB_REPOSITORY_NOT_FOUND,
        GITHUB_RATE_LIMITED,
        GITHUB_UPSTREAM_ERROR,
        GITHUB_TIMEOUT,
        REPOSITORY_ALREADY_CONNECTED,
        REPOSITORY_SYNC_ALREADY_RUNNING,
        EVENT_REPOSITORY_ACCESS_DENIED,
        TOKEN_ENCRYPTION_CONFIGURATION_ERROR,
        REPOSITORY_INTEGRATION_NOT_FOUND,
        SUBMISSION_NOT_FOUND,
        SUBMISSION_REPOSITORY_ACCESS_DENIED,
        SUBMISSION_REPOSITORY_MODIFICATION_NOT_ALLOWED,
        SUBMISSION_REPOSITORY_NOT_FOUND
    }

    private final ErrorCode errorCode;
    private final String retryAfter;

    public RepositoryIntegrationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.retryAfter = null;
    }

    public RepositoryIntegrationException(ErrorCode errorCode, String message, String retryAfter) {
        super(message);
        this.errorCode = errorCode;
        this.retryAfter = retryAfter;
    }

    public RepositoryIntegrationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryAfter = null;
    }
}
