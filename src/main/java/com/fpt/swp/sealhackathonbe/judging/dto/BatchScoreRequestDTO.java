package com.fpt.swp.sealhackathonbe.judging.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class BatchScoreRequestDTO {
    private List<UUID> submissionIds;
}
