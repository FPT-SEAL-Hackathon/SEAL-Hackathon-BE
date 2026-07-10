package com.fpt.swp.sealhackathonbe.team.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MilestoneResponse {
    private UUID milestoneId;
    private UUID teamId;
    private UUID mentorUserId;
    private String label;
    private Boolean isDone;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
