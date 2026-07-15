package com.fpt.swp.sealhackathonbe.submission.dto;

public final class SubmissionUrlPatterns {
    public static final String GITHUB_REPOSITORY_URL =
            "^$|https://github\\.com/[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?/[A-Za-z0-9._-]+(?:\\.git)?/?$";

    private SubmissionUrlPatterns() {
    }
}
