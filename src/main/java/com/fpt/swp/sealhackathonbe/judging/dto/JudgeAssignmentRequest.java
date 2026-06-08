package com.fpt.swp.sealhackathonbe.judging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeAssignmentRequest {
    private UUID judgeId;
    private UUID submissionId;
    private String submissionName;
    private UUID categoryId;
    private String categoryName;
    private Boolean isScored;
}
