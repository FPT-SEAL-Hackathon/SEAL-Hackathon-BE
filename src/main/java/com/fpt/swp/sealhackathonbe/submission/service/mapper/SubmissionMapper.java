package com.fpt.swp.sealhackathonbe.submission.service.mapper;

import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionHistoryResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionHistory;
import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionStatus;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;

import java.util.UUID;

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
        response.setSubmissionStatusId(submission.getSubmissionStatusId());
        response.setSubmissionStatusName(resolveStatusName(
                submission.getSubmissionStatusId(),
                submission.getSubmissionStatus()
        ));

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
        response.setIsScoreApproved(submission.getIsScoreApproved());
        response.setIsSampleSubmission(submission.getIsSampleSubmission());

        return response;
    }

    public static SubmissionHistoryResponse toSubmissionHistoryResponse(SubmissionHistory history) {
        if (history == null) {
            return null;
        }

        SubmissionHistoryResponse response = new SubmissionHistoryResponse();

        response.setSubmissionHistoryId(history.getSubmissionHistoryId());
        response.setSubmissionId(history.getSubmissionId());
        response.setVersionNumber(history.getVersionNumber());
        response.setTeamId(history.getTeam() != null
                ? history.getTeam().getTeamId()
                : history.getTeamId());
        response.setTeamName(history.getTeam() != null
                ? history.getTeam().getTeamName()
                : null);
        response.setRoundId(history.getRoundId());
        response.setSubmissionStatusId(history.getSubmissionStatusId());
        response.setSubmissionStatusName(resolveStatusName(
                history.getSubmissionStatusId(),
                history.getSubmissionStatus()
        ));

        response.setRepositoryUrl(history.getRepositoryUrl());
        response.setDemoUrl(history.getDemoUrl());
        response.setReportUrl(history.getReportUrl());
        response.setSlideUrl(history.getSlideUrl());

        response.setRepoMetadataJson(history.getRepoMetadataJson());
        response.setRepoLastCommitAt(history.getRepoLastCommitAt());
        response.setRepoStarCount(history.getRepoStarCount());
        response.setRepoForkCount(history.getRepoForkCount());

        response.setSubmittedAt(history.getSubmittedAt());
        response.setLastUpdatedAt(history.getLastUpdatedAt());
        response.setSubmittedByUserId(history.getSubmittedByUser() != null
                ? history.getSubmittedByUser().getUserId()
                : history.getSubmittedByUserId());
        response.setNotes(history.getNotes());
        response.setIsScoreApproved(history.getIsScoreApproved());
        response.setIsSampleSubmission(history.getIsSampleSubmission());
        response.setSnapshotCreatedAt(history.getSnapshotCreatedAt());

        return response;
    }

    private static String resolveStatusName(UUID statusId, SubmissionStatus status) {
        if (status != null && statusId != null && statusId.equals(status.getStatusId())) {
            return status.getStatusName();
        }

        if (SubmissionStatusConstants.DRAFT.equals(statusId)) {
            return "Draft";
        }
        if (SubmissionStatusConstants.SUBMITTED.equals(statusId)) {
            return "Submitted";
        }
        if (SubmissionStatusConstants.UNDER_REVIEW.equals(statusId)) {
            return "Under Review";
        }
        if (SubmissionStatusConstants.DISQUALIFIED.equals(statusId)) {
            return "Disqualified";
        }
        if (SubmissionStatusConstants.SCORED.equals(statusId)) {
            return "Scored";
        }
        if (SubmissionStatusConstants.IN_PROGRESS.equals(statusId)) {
            return "In Progress";
        }

        return status != null ? status.getStatusName() : null;
    }
}
