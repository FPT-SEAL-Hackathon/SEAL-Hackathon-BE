package com.fpt.swp.sealhackathonbe.submission.service.mapper;

import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionHistoryResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionHistory;
import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionStatus;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubmissionMapperTest {

    @Test
    void mapsLatestStatusIdWhenLoadedStatusRelationIsStale() {
        SubmissionStatus staleSubmittedStatus = new SubmissionStatus();
        staleSubmittedStatus.setStatusId(SubmissionStatusConstants.SUBMITTED);
        staleSubmittedStatus.setStatusName("Submitted");

        Submissions submission = new Submissions();
        submission.setSubmissionId(UUID.randomUUID());
        submission.setTeamId(UUID.randomUUID());
        submission.setRoundId(UUID.randomUUID());
        submission.setSubmissionStatusId(SubmissionStatusConstants.DISQUALIFIED);
        submission.setSubmissionStatus(staleSubmittedStatus);
        submission.setLastUpdatedAt(LocalDateTime.now());
        submission.setSubmittedByUserId(UUID.randomUUID());

        SubmissionResponse response = SubmissionMapper.toSubmissionResponse(submission);

        assertEquals(SubmissionStatusConstants.DISQUALIFIED, response.getSubmissionStatusId());
        assertEquals("Disqualified", response.getSubmissionStatusName());
    }

    @Test
    void mapsHistoryStatusFromSnapshotNotCurrentSubmission() {
        Submissions currentSubmission = new Submissions();
        currentSubmission.setSubmissionId(UUID.randomUUID());
        currentSubmission.setSubmissionStatusId(SubmissionStatusConstants.DISQUALIFIED);

        SubmissionHistory history = new SubmissionHistory();
        history.setSubmissionHistoryId(UUID.randomUUID());
        history.setSubmissionId(currentSubmission.getSubmissionId());
        history.setSubmission(currentSubmission);
        history.setVersionNumber(1);
        history.setTeamId(UUID.randomUUID());
        history.setRoundId(UUID.randomUUID());
        history.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        history.setLastUpdatedAt(LocalDateTime.now());
        history.setSubmittedByUserId(UUID.randomUUID());
        history.setSnapshotCreatedAt(LocalDateTime.now());

        SubmissionHistoryResponse response = SubmissionMapper.toSubmissionHistoryResponse(history);

        assertEquals(SubmissionStatusConstants.SUBMITTED, response.getSubmissionStatusId());
        assertEquals("Submitted", response.getSubmissionStatusName());
    }
}
