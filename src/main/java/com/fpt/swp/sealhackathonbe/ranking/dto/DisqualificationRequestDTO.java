package com.fpt.swp.sealhackathonbe.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisqualificationRequestDTO {
    private UUID teamId;
    private UUID submissionId;
    private String reason;
}
