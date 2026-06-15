package com.fpt.swp.sealhackathonbe.submission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DisqualifiedSubmissionResponse {
    // DTO cho man hinh admin: tra ID bai nop cung ly do, nguoi va thoi diem bi loai.
    private UUID disqualificationId;
    private UUID submissionId;
    private String reason;
    private UUID disqualifiedById;
    private LocalDateTime disqualifiedAt;
}
