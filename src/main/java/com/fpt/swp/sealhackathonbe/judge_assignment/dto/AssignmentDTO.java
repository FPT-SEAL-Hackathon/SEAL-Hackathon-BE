package com.fpt.swp.sealhackathonbe.judge_assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {
    private UUID judgeId;
    private UUID submissionId;
    private String submissionName;
    private UUID categoryId;
    private String categoryName;
    private Boolean isScored;
}
