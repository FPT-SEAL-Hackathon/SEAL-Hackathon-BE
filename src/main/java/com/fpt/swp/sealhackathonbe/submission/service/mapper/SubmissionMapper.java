package com.fpt.swp.sealhackathonbe.submission.service.mapper;

import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;

public class SubmissionMapper {
    private SubmissionMapper() {
    }

    public static SubmissionResponse toSubmissionResponse(Submissions submission) {
        // Ranh gioi mapping: JPA entity nam trong service layer, DTO duoc tra ve controller/client.
        if (submission == null) {
            return null;
        }

        SubmissionResponse response = new SubmissionResponse();

        response.setSubmissionId(submission.getSubmissionId());
        response.setTeamId(submission.getTeamId());
        response.setRoundId(submission.getRoundId());
        response.setSubmissionStatusId(submission.getSubmissionStatusId());

        response.setRepositoryUrl(submission.getRepositoryUrl());
        response.setDemoUrl(submission.getDemoUrl());
        response.setReportUrl(submission.getReportUrl());
        response.setSlideUrl(submission.getSlideUrl());

        response.setRepoMetadataJson(submission.getRepoMetadataJson());
        response.setRepoLastCommitAt(submission.getRepoLastCommitAt());
        response.setRepoStarCount(submission.getRepoStarCount());
        response.setRepoForkCount(submission.getRepoForkCount());

        response.setSubmittedAt(submission.getSubmittedAt());
        response.setLastUpdatedAt(submission.getLastUpdatedAt());
        response.setSubmittedByUserId(submission.getSubmittedByUserId());
        response.setNotes(submission.getNotes());

        return response;
    }
}
