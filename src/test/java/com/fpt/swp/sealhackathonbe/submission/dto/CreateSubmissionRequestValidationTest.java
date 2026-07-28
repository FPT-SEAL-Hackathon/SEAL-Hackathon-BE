package com.fpt.swp.sealhackathonbe.submission.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateSubmissionRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void repositoryUrlAcceptsStandardGithubRepositoryUrls() {
        assertRepositoryUrlValid("https://github.com/octocat/Hello-World");
        assertRepositoryUrlValid("https://github.com/fpt-swp/seal_hackathon.be/");
        assertRepositoryUrlValid("https://github.com/fpt-swp/seal-hackathon.git");
    }

    @Test
    void repositoryUrlRejectsNonGithubOrNonRepositoryUrls() {
        assertRepositoryUrlInvalid("http://github.com/octocat/Hello-World");
        assertRepositoryUrlInvalid("https://gitlab.com/octocat/Hello-World");
        assertRepositoryUrlInvalid("https://github.com/octocat");
        assertRepositoryUrlInvalid("https://github.com/octocat/Hello-World/tree/main");
        assertRepositoryUrlInvalid("https://github.com/-octocat/Hello-World");
    }

    private void assertRepositoryUrlValid(String repositoryUrl) {
        assertTrue(repositoryUrlViolations(repositoryUrl).isEmpty());
    }

    private void assertRepositoryUrlInvalid(String repositoryUrl) {
        assertFalse(repositoryUrlViolations(repositoryUrl).isEmpty());
    }

    private Set<ConstraintViolation<CreateSubmissionRequest>> repositoryUrlViolations(String repositoryUrl) {
        CreateSubmissionRequest request = new CreateSubmissionRequest();
        request.setTeamId(UUID.randomUUID());
        request.setRoundId(UUID.randomUUID());
        request.setRepositoryUrl(repositoryUrl);

        return validator.validate(request);
    }
}
