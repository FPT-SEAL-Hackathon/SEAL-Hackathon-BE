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
        response.setTeamId(submission.getTeam() != null
                ? submission.getTeam().getTeamId()
                : submission.getTeamId());
        response.setTeamName(submission.getTeam() != null
                ? submission.getTeam().getTeamName()
                : null);
        response.setRoundId(submission.getRoundId());
        response.setSubmissionStatusId(submission.getSubmissionStatus() != null
                ? submission.getSubmissionStatus().getStatusId()
                : submission.getSubmissionStatusId());
        response.setSubmissionStatusName(submission.getSubmissionStatus() != null
                ? submission.getSubmissionStatus().getStatusName()
                : null);

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
        response.setSubmittedByUserId(submission.getSubmittedByUser() != null
                ? submission.getSubmittedByUser().getUserId()
                : submission.getSubmittedByUserId());
        response.setNotes(submission.getNotes());

        return response;
    }
}
