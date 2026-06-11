package com.fpt.swp.sealhackathonbe.submission.service.mapper;

import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;

public class SubmissionDisqualificationMapper {
    private SubmissionDisqualificationMapper() {
    }

    public static SubmissionDisqualificationResponse toResponse(Disqualifications disqualification) {
        SubmissionDisqualificationResponse response = new SubmissionDisqualificationResponse();
        response.setDisqualificationId(disqualification.getDisqualificationId());
        response.setSubmissionId(disqualification.getSubmissionId());
        response.setReason(disqualification.getReason());
        response.setDisqualifiedById(disqualification.getDisqualifiedById());
        response.setDisqualifiedAt(disqualification.getDisqualifiedAt());
        response.setReversed(disqualification.getReversed());
        return response;
    }

    public static DisqualifiedSubmissionResponse toDisqualifiedSubmissionResponse(
            Disqualifications disqualification
    ) {
        DisqualifiedSubmissionResponse response = new DisqualifiedSubmissionResponse();
        response.setDisqualificationId(disqualification.getDisqualificationId());
        response.setSubmission(SubmissionMapper.toSubmissionResponse(disqualification.getSubmission()));
        response.setReason(disqualification.getReason());
        response.setDisqualifiedById(disqualification.getDisqualifiedById());
        response.setDisqualifiedAt(disqualification.getDisqualifiedAt());
        return response;
    }
}
