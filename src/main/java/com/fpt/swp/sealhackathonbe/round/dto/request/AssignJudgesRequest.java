package com.fpt.swp.sealhackathonbe.round.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AssignJudgesRequest {
    private List<UUID> userIds;
}
