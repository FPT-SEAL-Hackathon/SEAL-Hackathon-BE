package com.fpt.swp.sealhackathonbe.integration.repository.exception;

import lombok.Getter;

@Getter
public class RepositoryMetadataException extends RuntimeException {

    private final RepositoryMetadataErrorCode errorCode;
    private final Integer httpStatus;
    private final String requestId;
    private final String rateLimitRemaining;
    private final String repositoryFullName;

    public RepositoryMetadataException(RepositoryMetadataErrorCode errorCode, String message) {
        this(errorCode, message, null, null, null, null, null);
    }

    public RepositoryMetadataException(RepositoryMetadataErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, null, null, null, null);
    }

    public RepositoryMetadataException(
            RepositoryMetadataErrorCode errorCode,
            String message,
            Throwable cause,
            Integer httpStatus,
            String requestId,
            String rateLimitRemaining,
            String repositoryFullName) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.requestId = requestId;
        this.rateLimitRemaining = rateLimitRemaining;
        this.repositoryFullName = repositoryFullName;
    }
}
