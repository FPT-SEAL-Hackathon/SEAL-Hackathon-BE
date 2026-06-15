package com.fpt.swp.sealhackathonbe.submission.service.mapper;

import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;

public class SubmissionDisqualificationMapper {
    private SubmissionDisqualificationMapper() {
    }

    public static SubmissionDisqualificationResponse toResponse(Disqualifications disqualification) {
        // Response gon cho ket qua ngay sau khi admin loai mot submission.
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
        // Danh sach chi can SubmissionID, khong expose toan bo SubmissionResponse long ben trong.
        DisqualifiedSubmissionResponse response = new DisqualifiedSubmissionResponse();
        response.setDisqualificationId(disqualification.getDisqualificationId());
        response.setSubmissionId(disqualification.getSubmissionId());
        response.setReason(disqualification.getReason());
        response.setDisqualifiedById(disqualification.getDisqualifiedById());
        response.setDisqualifiedAt(disqualification.getDisqualifiedAt());
        return response;
    }
}
