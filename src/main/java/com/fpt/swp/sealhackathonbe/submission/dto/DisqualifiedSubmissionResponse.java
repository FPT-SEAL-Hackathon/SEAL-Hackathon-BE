package com.fpt.swp.sealhackathonbe.submission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DisqualifiedSubmissionResponse {
    // DTO cho man hinh admin: ghep submission voi ly do, nguoi va thoi diem bi loai.
    private UUID disqualificationId;
    private SubmissionResponse submission;
    private String reason;
    private UUID disqualifiedById;
    private LocalDateTime disqualifiedAt;
}
