package com.fpt.swp.sealhackathonbe.submission.service.mapper;

import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
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
}
