package com.fpt.swp.sealhackathonbe.submission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubmissionDisqualificationResponse {
    // Response cua luong loai submission: target la SubmissionID, TeamID trong Disqualifications de null.
    private UUID disqualificationId;
    private UUID submissionId;
    private String reason;
    private UUID disqualifiedById;
    private LocalDateTime disqualifiedAt;
    private Boolean reversed;
}
