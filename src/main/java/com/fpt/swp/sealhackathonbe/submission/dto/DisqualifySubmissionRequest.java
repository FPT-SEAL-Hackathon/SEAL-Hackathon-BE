package com.fpt.swp.sealhackathonbe.submission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisqualifySubmissionRequest {
    // Admin nhap ly do loai rieng bai nop; team van khong bi loai neu chi submission nay sai rule.
    @NotBlank(message = "Reason is required")
    private String reason;
}
