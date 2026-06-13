package com.fpt.swp.sealhackathonbe.round.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RoundJudgeResponse {
    private UUID roundJudgeId;
    private UUID roundId;
    private UUID judgeId;
    private LocalDateTime assignedAt;
    private UUID assignedById;
}
